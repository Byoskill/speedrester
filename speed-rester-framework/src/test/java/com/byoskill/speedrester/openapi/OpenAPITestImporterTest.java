package com.byoskill.speedrester.openapi;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OpenAPITestImporterTest {

    @Test
    public void testGeneration() throws IOException {
        final OpenAPITestImporter openAPITestImporter = new OpenAPITestImporter();
        final Path                generationFolder    = Files.createTempDirectory("swagger");
        openAPITestImporter.generateTests(getClass().getResource("/examples/openapi.json"), generationFolder.toFile());

        final File serverConfigurationFile = new File(generationFolder.toFile().getAbsolutePath(), OpenAPITestImporter.SERVER_CONFIGURATION_JSON);
        Assertions.assertThat(serverConfigurationFile).exists();
    }


    @Test
    public void testGenerationPetStore() throws IOException {
        final OpenAPITestImporter openAPITestImporter = new OpenAPITestImporter();
        final File                generationFolder    = new File("src/test/resources/petstore");
        openAPITestImporter.generateTests(getClass().getResource("/examples/petstore.json"), generationFolder);

        final File serverConfigurationFile = new File(generationFolder.getAbsolutePath(), OpenAPITestImporter.SERVER_CONFIGURATION_JSON);
        Assertions.assertThat(serverConfigurationFile).exists();
    }

}