package com.byoskill.speedrester.execution;

import com.byoskill.speedrester.model.RestCallStep;
import com.byoskill.speedrester.model.RestScenario;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.impl.client.CloseableHttpClient;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class HttpSequenceExecutor extends HttpSingleExecutionExecutor {

    public HttpSequenceExecutor(final RestScenario restScenario, final CloseableHttpClient httpclient, final ServerConfiguration serverConfiguration) {
        super(restScenario, httpclient, serverConfiguration);
    }

    public CloseableHttpResponse execute() throws IOException, URISyntaxException {
        log.info("Multiple step execution");
        CloseableHttpResponse    response = null;
        final List<RestCallStep> sequence = this.restScenario.getSequence();
        for (int i = 0, sequenceSize = sequence.size(); i < sequenceSize; i++) {
            try {
                final RestCallStep restCall = sequence.get(i);
                this.initScopeParams(restCall);
                log.info("    > Execution of the step {}", restCall.getStepName());
                final HttpRequestBase requestBase = this.buildRequest(restCall);
                this.injectHeaders(requestBase, restCall);
                response = this.executeHttpRequest(this.httpclient, requestBase);
                if (response == null) throw new IOException("No response from the server, value is null");
                if (i < (sequenceSize - 1)) {
                    this.scopeParams = this.processResponseForParams(response, restCall);
                }
            } finally {
                if (i < (sequenceSize - 1)) {
                    if (response != null) {
                        response.close();
                    }
                }
            }
        }
        return response;
    }

    private Map<String, String> processResponseForParams(final CloseableHttpResponse response, final RestCallStep restCall) throws IOException {
        final Map<String, String> scopeVariables = new HashMap<>(this.scopeParams);
        restCall.getHeaderVariables().forEach((varName, headerName) -> {
            final Header header = response.getFirstHeader(headerName);
            if (header != null) {
                scopeVariables.put(varName, header.getValue());
            } else {
                scopeVariables.put(varName, null);
            }
        });

        if (response.getEntity() != null) {
            final String content = IOUtils.toString(response.getEntity().getContent(), StandardCharsets.UTF_8);
            log.debug("Received content {}", content);

            restCall.getJsonPathVariable().forEach((varName, jsonPath) -> {
                final Object jsonPathValue = JsonPath.read(content, jsonPath);
                final String value         = Objects.toString(jsonPathValue);
                scopeVariables.put(varName, value);
            });

            if (restCall.getJavacript() != null && !restCall.getJavacript().isBlank()) {
                this.evaluateJavascriptCode(content, restCall.getJavacript(), Map.of());
            }
        }

        log.warn("Scope variables for the next call are : {}", scopeVariables);
        return scopeVariables;
    }

    private void evaluateJavascriptCode(final String content, final String javascript, final Map<String, Object> scopeVariables) {
        final Context cx = Context.enter();
        try {
            final Scriptable scope = cx.initStandardObjects();

            scopeVariables.forEach((k, v) -> ScriptableObject.putProperty(scope, k, Context.javaToJS(v, scope)));

            ScriptableObject.putProperty(scope, "content", Context.javaToJS(content, scope));
            ScriptableObject.putProperty(scope, "restScenario", Context.javaToJS(this.restScenario, scope));


            final Object result = cx.evaluateString(scope, javascript, "<cmd>", 1, null);
            System.out.println(result);
        } finally {
            Context.exit();
        }
    }
}
