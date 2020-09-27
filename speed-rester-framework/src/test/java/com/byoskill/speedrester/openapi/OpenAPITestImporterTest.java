package com.byoskill.speedrester.openapi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

public class OpenAPITestImporterTest {

    @Disabled
    @Test
    public void testGeneration() throws IOException {
        final OpenAPITestImporter openAPITestImporter = new OpenAPITestImporter();
        final String              generationFolder    = "src/test/resources/paymentCenterAPI";
        openAPITestImporter.generateTests(new URL("http://localhost:8081/rest/openapi.json"), new File(generationFolder));

        final File serverConfigurationFile = new File(generationFolder, OpenAPITestImporter.SERVER_CONFIGURATION_JSON);
        Assertions.assertThat(serverConfigurationFile).exists();
    }

}