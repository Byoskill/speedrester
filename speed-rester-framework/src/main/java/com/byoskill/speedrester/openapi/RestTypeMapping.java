package com.byoskill.speedrester.openapi;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class RestTypeMapping {
    private String                     typeName;
    private Map<String, RestTypeField> fields = new HashMap<>();

    public void addField(RestTypeField restTypeField) {
        this.fields.put(restTypeField.getName(), restTypeField);
    }
}
