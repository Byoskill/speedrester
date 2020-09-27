package com.byoskill.speedrester.openapi;

import lombok.Data;

@Data
public class RestTypeField {
    private String  name;
    private String  basicType;
    private RefName reference;
    private boolean array;

    public void setArray() {
        this.array = true;
    }

    public boolean hasReference() {
        return this.reference != null;
    }
}
