package com.byoskill.speedrester.execution;

import java.io.InputStream;

@FunctionalInterface
public interface PayloadFileResolver {

    InputStream fetch(String filePath);
}
