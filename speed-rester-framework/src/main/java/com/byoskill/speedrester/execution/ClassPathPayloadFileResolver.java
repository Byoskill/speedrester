package com.byoskill.speedrester.execution;

import java.io.InputStream;

public class ClassPathPayloadFileResolver implements PayloadFileResolver {

    private final String relativePath;

    public ClassPathPayloadFileResolver() {
        this.relativePath = "";
    }

    public ClassPathPayloadFileResolver(final String relativePath) {
        this.relativePath = relativePath;
    }

    @Override
    public InputStream fetch(final String filePath) {
        final String name = this.relativePath + filePath;
        return this.getClass().getResourceAsStream(name);
    }
}
