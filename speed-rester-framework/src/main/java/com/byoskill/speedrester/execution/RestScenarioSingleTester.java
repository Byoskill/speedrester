package com.byoskill.speedrester.execution;

import com.byoskill.speedrester.model.RestExpectations;
import com.byoskill.speedrester.model.RestScenario;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.methods.*;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.IOException;
import java.net.URISyntaxException;

@Slf4j
public class RestScenarioSingleTester {
    private final RestScenario        restScenario;
    private final ServerConfiguration serverConfiguration;

    public RestScenarioSingleTester(final RestScenario restScenario, final ServerConfiguration serverConfiguration) {
        this.restScenario        = restScenario;
        this.serverConfiguration = serverConfiguration;

    }

    public ScenarioTestResult test() {
        try (final CloseableHttpClient httpclient = this.initHttpClient()) {
            CloseableHttpResponse httpResponse = null;
            if (this.restScenario.getSequence() != null && !this.restScenario.getSequence().isEmpty()) {
                log.info("We are executing a single call...");
                final HttpSequenceExecutor httpSingleExecutionExecutor = new HttpSequenceExecutor(this.restScenario, httpclient, this.serverConfiguration);
                httpResponse = httpSingleExecutionExecutor.execute();
            } else {
                log.info("We are executing a rest sequence...");
                final HttpSingleExecutionExecutor httpSingleExecutionExecutor = new HttpSingleExecutionExecutor(this.restScenario, httpclient, this.serverConfiguration);
                httpResponse = httpSingleExecutionExecutor.execute();
            }

            try (final CloseableHttpResponse response1 = httpResponse) {
                return this.proceedResponse(response1);
            }
        } catch (final IOException | URISyntaxException e) {
            log.error("The scenario has failed for the reason {}", e.getMessage(), e);
            return ScenarioTestResult.fail(e);
        }
    }


    private CloseableHttpClient initHttpClient() {
        return HttpClients.createDefault();
    }

    private ScenarioTestResult proceedResponse(final CloseableHttpResponse response) throws IOException {
        final RestExpectations expectations     = this.restScenario.getExpectations();
        final AssertionChecker assertionChecker = new AssertionChecker(this.restScenario, expectations, response);
        return serverConfiguration.isIgnoreFailures() ? ScenarioTestResult.SUCCESS : assertionChecker.check();


    }

}
