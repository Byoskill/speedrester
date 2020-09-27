package com.byoskill.speedrester.execution;

import com.byoskill.speedrester.model.RestCall;
import com.byoskill.speedrester.model.RestExpectations;
import com.jayway.jsonpath.JsonPath;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.assertj.core.api.Assertions;
import org.json.JSONException;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.skyscreamer.jsonassert.JSONAssert;
import org.skyscreamer.jsonassert.JSONCompareMode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * THis class in in charge to check the different asasertions of a Rest scenario and to update the {@link ScenarioTestResult} accordingly;
 */
@Slf4j
public class AssertionChecker {
    private final ScenarioTestResult    scenarioTestResult;
    private final RestCall              restScenario;
    private final RestExpectations      expectations;
    private final CloseableHttpResponse response;

    public AssertionChecker(final RestCall restScenario, final RestExpectations expectations, final CloseableHttpResponse response) {

        this.restScenario       = restScenario;
        this.expectations       = expectations;
        this.response           = response;
        this.scenarioTestResult = new ScenarioTestResult();

    }

    public ScenarioTestResult check() throws IOException {

        this.checkHttpCode();
        if (this.response.getEntity() != null) {
            final String content = IOUtils.toString(this.response.getEntity().getContent(), StandardCharsets.UTF_8);
            log.debug("Received content {}", content);
            this.checkWholeContent(content);
            this.checkContainsAssertions(content);
            this.checkRexpAssertions(content);
            this.checkJsonAssertions(content);

            this.checkJsonPath(content);

            if (this.expectations.hasJavascript()) {
                this.checkJavascript(content);
            }
        } else {
            log.debug("Did not receive content from the server");
        }

        return this.scenarioTestResult;
    }

    private void checkHttpCode() {
        this.scenarioTestResult.evaluate("Http Status code", () -> Assertions.assertThat(this.response.getStatusLine().getStatusCode()).isEqualTo(this.expectations
                .getExpectedStatus().getCode()));
    }

    private void checkWholeContent(final String content) {
        if (this.expectations.getContent() != null) {
            this.scenarioTestResult.evaluate("Content should be equals to " + this.expectations.getContent(), () -> Assertions.assertThat(content).isEqualToIgnoringWhitespace(this.expectations.getContent()));
        }
    }

    private void checkContainsAssertions(final String content) {
        this.expectations.getContainsAssertions().forEach(
                assertion -> {
                    this.scenarioTestResult.evaluate("Payload should contain " + assertion, () -> Assertions.assertThat(content).withFailMessage("Payload should contains " + assertion).contains(assertion));
                }
        );
    }

    private void checkRexpAssertions(final String content) {
        this.expectations.getRegexpAssertions().forEach(
                assertion -> {
                    this.scenarioTestResult.evaluate("Payload should match the regexp " + assertion, () -> Assertions.assertThat(content).withFailMessage("Payload should match " + assertion).matches(assertion));
                }
        );
    }

    private void checkJsonAssertions(final String content) {
        this.expectations.getJsonAsserts().forEach(
                assertion -> {
                    this.scenarioTestResult.evaluate("JSON Payload should match the " + assertion, () -> {
                        try {
                            JSONAssert.assertEquals("JSON Payload should match ", assertion, content, JSONCompareMode.LENIENT);
                        } catch (final JSONException e) {
                            throw new RuntimeException("JSON Assertion has failed", e);
                        }
                    });
                }
        );
    }

    private void checkJsonPath(final String content) {
        this.expectations.getJsonPath().forEach(
                (path, value) -> {

                    final var javascriptComparison = value;
                    final var jsScript             = "/* " + path + " */ jsonPathResult " + javascriptComparison + ";";

                    final Object jsonFInding = JsonPath.read(content, path);
                    this.evaluateJavascriptCode(content, jsScript, Map.of("jsonPathResult", jsonFInding));
                }
        );
    }

    private void checkJavascript(final String content) {
        final String javascript = this.expectations.getJavascript();
        this.evaluateJavascriptCode(content, javascript, Map.of());
    }

    private void evaluateJavascriptCode(final String content, final String javascript, final Map<String, Object> scopeVariables) {
        final Context cx = Context.enter();
        try {
            final Scriptable scope = cx.initStandardObjects();

            scopeVariables.forEach((k, v) -> ScriptableObject.putProperty(scope, k, Context.javaToJS(v, scope)));

            ScriptableObject.putProperty(scope, "content", Context.javaToJS(content, scope));
            ScriptableObject.putProperty(scope, "response", Context.javaToJS(this.response, scope));
            ScriptableObject.putProperty(scope, "restScenario", Context.javaToJS(this.restScenario, scope));


            final Object result = cx.evaluateString(scope, javascript, "<cmd>", 1, null);
            final Runnable runnable = () -> {
                final Boolean parseJSResult = (Boolean) Context.jsToJava(result, Boolean.class);
                Assertions.assertThat(parseJSResult)
                        .withFailMessage("Javascript evaluation should returns true" + javascript)
                        .isEqualTo(Boolean.TRUE);
            };
            this.scenarioTestResult.evaluate("Javascript evaluation() : " + javascript, runnable);
        } finally {
            Context.exit();
        }
    }
}
