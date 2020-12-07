package com.byoskill.speedrester;

import com.byoskill.speedrester.execution.ClassPathPayloadFileResolver;
import com.byoskill.speedrester.execution.ServerConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScenarioJUnitScannerTest {


    @DisplayName("Testing Spring PetStore")
    @TestFactory
    List<DynamicTest> testPaymentCenterRESTAPI() throws IOException {
        final ServerConfiguration serverConfiguration = ServerConfiguration.load(this.getClass().getResourceAsStream("/petstore/test.server.configuration.json"));
        serverConfiguration.setPayloadFileResolver(new ClassPathPayloadFileResolver("/petstore/"));
        serverConfiguration.setIgnoreFailures(true);

        final ScenarioJUnitScanner scenarioJUnitScanner = new ScenarioJUnitScanner(serverConfiguration);

        return scenarioJUnitScanner.scanFolder("src/test/resources/petstore/tests");
    }


}