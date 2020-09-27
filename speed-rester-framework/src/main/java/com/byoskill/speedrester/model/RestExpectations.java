package com.byoskill.speedrester.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RestExpectations {
    private String              httpCode;
    private Integer             expectedBytes;
    private String              content;
    private List<String>        regexpAssertions   = new ArrayList<>();
    private List<String>        containsAssertions = new ArrayList<>();
    private List<String>        jsonAsserts        = new ArrayList<>();
    private Map<String, String> jsonPath           = new HashMap<>();
    private String              javascript;

    public void addJsonAssert(final String assertion) {
        this.jsonAsserts.add(assertion);
    }

    public boolean isExpectedHttpCode(final int statusCode) {
        return this.getExpectedStatus().getCode() == statusCode;
    }

    @JsonIgnore
    public HttpStatusCode getExpectedStatus() {
        if (StringUtils.isNumeric(this.httpCode)) {
            return HttpStatusCode.getCode(Integer.parseInt(this.httpCode));
        } else {
            return HttpStatusCode.valueOf(this.httpCode);
        }
    }

    public void addContainAssert(final String containsExpression) {
        this.containsAssertions.add(containsExpression);
    }

    public boolean hasJavascript() {
        return this.javascript != null && !this.javascript.isBlank();
    }

    @JsonIgnore
    public void setHttpStatusCode(final HttpStatusCode code) {
        this.httpCode = code.name();
    }
}
