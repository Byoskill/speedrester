package com.byoskill.speedrester.execution;

import com.byoskill.speedrester.model.RestCall;
import com.byoskill.speedrester.model.RestScenario;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.text.StrSubstitutor;
import org.apache.commons.text.StringSubstitutor;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HttpSingleExecutionExecutor {
    protected final RestScenario        restScenario;
    protected final CloseableHttpClient httpclient;
    protected final ServerConfiguration serverConfiguration;
    protected       Map<String, String> scopeParams = new HashMap<>();

    public HttpSingleExecutionExecutor(final RestScenario restScenario, final CloseableHttpClient httpclient, final ServerConfiguration serverConfiguration) {
        this.restScenario        = restScenario;
        this.httpclient          = httpclient;
        this.serverConfiguration = serverConfiguration;

    }

    public CloseableHttpResponse execute() throws IOException, URISyntaxException {
        final RestCall restCall = this.restScenario;
        this.initScopeParams(restCall);
        final HttpRequestBase requestBase = this.buildRequest(restCall);
        this.injectHeaders(requestBase, restCall);

        return this.executeHttpRequest(this.httpclient, requestBase);
    }

    protected void initScopeParams(final RestCall restCall) {
        this.pushScopeVariables(restCall.getPathParams());
        this.pushScopeVariables(restCall.getQueryParams());
        this.pushScopeVariables(this.restScenario.getVariables());
        this.pushScopeVariables(this.serverConfiguration.getParams());
    }

    protected HttpRequestBase buildRequest(final RestCall restCall) throws URISyntaxException, JsonProcessingException {
        final URI resourcePath = this.buildResourcePath(restCall);


        switch (restCall.getMethod()){
            case GET:
                return new HttpGet(resourcePath);
            case PUT:{
                final HttpPut httpPut = new HttpPut(resourcePath);
                this.injectPayLoad(restCall, httpPut);
                return httpPut;
            }
            case POST:{
                final HttpPost httpPost = new HttpPost(resourcePath);
                this.injectPayLoad(restCall, httpPost);
                return httpPost;
            }
            case PATCH:{
                final HttpPatch httpPatch = new HttpPatch(resourcePath);
                this.injectPayLoad(restCall, httpPatch);
                return httpPatch;
            }
            case DELETE:
                final HttpDelete httpDelete = new HttpDelete(resourcePath);
                return httpDelete;
            case HEAD:
                return new HttpHead(resourcePath);

            case OPTIONS:
                break;
        }
        throw new UnsupportedOperationException("Unsupported method yet " + restCall.getMethod());
    }

    protected void injectHeaders(final HttpRequestBase requestBase, final RestCall restCall) {
        requestBase.addHeader("Content-Type", restCall.getContentType());
        restCall.getHeaders().forEach((k, v) -> requestBase.addHeader(k, this.interpolate(v)));
        log.info("Final query {}", requestBase);
        log.info("Headers {}", Arrays.toString(requestBase.getAllHeaders()));
    }

    protected CloseableHttpResponse executeHttpRequest(final CloseableHttpClient httpclient, final HttpRequestBase httpRequest) throws IOException {
        return httpclient.execute(httpRequest);
    }

    private void pushScopeVariables(final Map<String, String> params) {
        params.forEach((k, v) -> {
            if (v != null && !this.scopeParams.containsKey(k)) {
                this.scopeParams.put(k, v);
            }
        });
    }

    protected URI buildResourcePath(final RestCall restCall) throws URISyntaxException {
        final String rawResourcePath   = restCall.getResourcePath();
        final String buildResourcePath = this.interpolate(rawResourcePath);
        log.info("> Resource path {}", buildResourcePath);
        final URIBuilder uriBuilder1 = new URIBuilder(this.serverConfiguration.getBaseURL());

        // Concatenate resource path
        final String     path       = uriBuilder1.getPath();
        final URIBuilder uriBuilder = uriBuilder1.setPath(path == null ? buildResourcePath : (path + buildResourcePath));
        // Inject query params
        restCall.getQueryParams().forEach((k, v) -> {
            if (v == null) { // Override with scope params
                uriBuilder.addParameter(k, this.scopeParams.get(k));
            } else {
                uriBuilder.addParameter(k, this.interpolate(v));
            }
        });

        final URI consumedUrl = uriBuilder.build();
        log.info("> URL {}", consumedUrl);

        return consumedUrl;
    }

    private void injectPayLoad(final RestCall restCall, final HttpEntityEnclosingRequestBase httpRequest) throws JsonProcessingException {
        final RestScenario.RestScenarioPayload payload = restCall.getPayload();
        if (payload == null) return;
        if (payload.getFilePath() != null) {
            final InputStream resourceAsStream = this.serverConfiguration.getPayloadFileResolver().fetch(payload.getFilePath());
            httpRequest.setEntity(new InputStreamEntity(resourceAsStream));
        } else {
            final String stringpPayload = payload.convertAsString();
            httpRequest.setEntity(new StringEntity(stringpPayload, StandardCharsets.UTF_8));

        }
    }

    protected String interpolate(final String stringToInterpolate) {
        final StringSubstitutor strSubstitutor = new StringSubstitutor(this.scopeParams);
        return strSubstitutor.replace(stringToInterpolate);
    }

    protected String extractPayload(final RestCall restCall) throws JsonProcessingException {
        final String content = restCall.getPayload().convertAsString();
        final String payload = this.interpolate(content);
        log.info("  > Payload is\n{}", payload);
        return payload;
    }
}
