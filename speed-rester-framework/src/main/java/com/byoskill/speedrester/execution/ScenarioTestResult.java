package com.byoskill.speedrester.execution;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Data
@Slf4j
public class ScenarioTestResult {
    public static ScenarioTestResult SUCCESS = new ScenarioTestResult();

    private Throwable           errorReason;
    private boolean             success       = true;
    private Map<String, String> errorMessages = new HashMap<>();

    public static ScenarioTestResult fail(final Throwable e) {
        final ScenarioTestResult scenarioTestResult = new ScenarioTestResult();
        scenarioTestResult.setErrorReason(e);
        scenarioTestResult.setSuccess(false);
        return scenarioTestResult;
    }

    public void evaluate(final String message, final Runnable evaluation) {
        try {
            evaluation.run();
            this.errorMessages.put(message, "SUCCESS");
        } catch (Throwable t) {
            log.error("Assertion {} as failed", message, t);
            this.errorMessages.put(message, "FAILURE");
            setSuccess(false);
        }
    }
}
