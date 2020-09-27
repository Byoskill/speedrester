package com.byoskill.speedrester.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RestCall {
    protected String                           resourcePath;
    protected Map<String, String>              pathParams  = new HashMap<>();
    protected Map<String, String>              headers     = new HashMap<>();
    private   HttpMethodEnum                   method;
    private   String                           contentType;
    private   RestScenario.RestScenarioPayload payload     = new RestScenario.RestScenarioPayload();
    private   RestScenario.RestScenarioOptions options     = new RestScenario.RestScenarioOptions();
    private   Map<String, String>              queryParams = new HashMap<>();


    public void addHeader(final String headerKey, final String headerValue) {
        this.headers.put(headerKey, headerValue);
    }

}
