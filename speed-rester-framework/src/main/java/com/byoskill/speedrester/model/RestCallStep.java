package com.byoskill.speedrester.model;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RestCallStep extends RestCall {

    private String stepName;
    private final Map<String, String> headerVariables  = new HashMap();
    private final Map<String, String> jsonPathVariable = new HashMap();
    private       String              javacript;


}
