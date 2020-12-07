package com.byoskill.speedrester.execution;

import com.byoskill.speedrester.utils.JsonUtils;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Data
public class ServerConfiguration {

    private           String              baseURL;
    private           Map<String, String> params              = new HashMap<>();
    private           int                 defaultTimeOut      = 5000;
    @JsonIgnore
    private transient PayloadFileResolver payloadFileResolver = new ClassPathPayloadFileResolver();
    private           boolean             pingEnabled         = true;

    public static ServerConfiguration load(final InputStream resourceAsStream) throws IOException {
        return JsonUtils.INSTANCE.readValue(resourceAsStream, ServerConfiguration.class);
    }

    public Map<String, String> getParams() {
        return this.params == null ? Map.of() : this.params;
    }

    public int getDefaultTimeOut() {
        return this.defaultTimeOut;
    }

    public void setDefaultTimeOut(final int defaultTimeOut) {
        this.defaultTimeOut = defaultTimeOut;
    }

    public void putParam(final String variableName, final String typeExampleValue) {
        this.params.put(variableName, typeExampleValue);
    }

    public boolean isPingEnabled() {
        return pingEnabled;
    }
}
