package com.byoskill.speedrester.execution;

import com.byoskill.speedrester.model.RestCall;
import com.byoskill.speedrester.model.RestScenario;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.assertj.core.api.Assertions;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class RestScenarioExecutor {

    private final List<RestScenario>                scenarios = new ArrayList<>();
    private final ServerConfiguration               serverConfiguration;
    private final Map<RestCall, ScenarioTestResult> results = new LinkedHashMap<>();

    public RestScenarioExecutor(final ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;

    }

    public void addTest(final RestScenario restScenario) {
        Validate.isTrue(restScenario.hasTestName());
        log.info("Adding test \"{}\"", restScenario.getTestName());
        this.scenarios.add(restScenario);

    }

    public void run() {
        log.info("---------------------------------------------");
        log.info("Execution of REST tests");
        log.info("");
        for (final RestScenario restScenario : this.scenarios) {
            if (restScenario.isDisabled()) {
                log.warn("Scenario {} is disabled", restScenario.isDisabled());
                continue ;
            }
            this.executeScenario(restScenario);
        }
        log.info("---------------------------------------------");
        log.info("Execution finished");
        log.info("Number of tests {}", this.scenarios.size());
        final long failures = this.computeFailures();
        log.error("Number of failed tests {}", failures);


    }

    private void executeScenario(final RestScenario restScenario) {
        log.info(" > Scenario \"{}\" to be tested", restScenario.getTestName());

        final RestScenarioSingleTester restScenarioSingleTester = new RestScenarioSingleTester(restScenario, this.serverConfiguration);
        final ScenarioTestResult       scenarioTestResult       = restScenarioSingleTester.test();
        this.results.put(restScenario, scenarioTestResult);
        scenarioTestResult.getErrorMessages().forEach((k, v) -> {
            log.info("   Assertion \"{}\" : {}", k, v);
        });
        log.info(" > TEST RESULT : {}", scenarioTestResult.isSuccess() ? "SUCCESS" : "FAILURE");
        Assertions.assertThat(scenarioTestResult.isSuccess()).withFailMessage("Test " + restScenario.getTestName() + " has failed").isTrue();


    }

    private long computeFailures() {
        return this.results.values().stream().filter(r -> !r.isSuccess()).count();
    }
}
