package com.byoskill.speedrester;

import com.byoskill.speedrester.execution.RestScenarioExecutor;
import com.byoskill.speedrester.execution.ServerConfiguration;
import com.byoskill.speedrester.model.RestScenario;
import com.byoskill.speedrester.scanner.FileTestScanner;
import com.byoskill.speedrester.scanner.ResourceTest;
import com.byoskill.speedrester.scanner.ResourceTestScanner;
import com.byoskill.speedrester.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DynamicTest;

import javax.annotation.Nullable;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This scanner will search for the scenarios to test your REST API .
 * It will produce what it called dynamic tests for JUnit 5.
 * The Scanner uses some configuration to initializes and instantiate the tests.
 * Especially :
 *  <ul>
 *      <li>A server configuration</li>*
 *  </ul>
 */
@Slf4j
public class ScenarioJUnitScanner {

    private final ServerConfiguration serverConfiguration;
    private       ResourceTestScanner resourceTestScanner;

    /**
     * Builds a JUNIT Scanner that scans a folder for the presence of tests
     *
     * @param serverConfiguration the server configuration
     * @return the list of dynamic tests
     */
    public static List<DynamicTest> fileScanner(ServerConfiguration serverConfiguration, Path testFolder) throws IOException {
        return new ScenarioJUnitScanner(serverConfiguration, new FileTestScanner(testFolder)).scanFolder();
    }

    /**
     * Scan a folder and reads for the presence of rest tests. The tests have the JSON extension.
     *
     * @param packageName the folder path.
     * @return list of dynamic tests
     * @throws IOException the io exception
     */
    public List<DynamicTest> scanFolder() throws IOException {
        final String baseURL = this.serverConfiguration.getBaseURL();
        if (baseURL == null) {
            log.error("Tests are disabled, no server url is provided");
            return List.of();
        }
        if (serverConfiguration.isPingEnabled()) {
            if (disableIfServerIsUnavailable(baseURL)) return List.of();
        }
        var testResources = resourceTestScanner.findTests();
        return testResources.map(this::generateJUnitTest)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());
    }

    /**
     * This method returns true if the server does not repond
     *
     * @param baseURL the base URL
     * @return true if the serer does not respond.
     */
    private boolean disableIfServerIsUnavailable(final String baseURL) {
        final URL url = new URL(baseURL);
        try {
            final URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(this.serverConfiguration.getDefaultTimeOut());
            urlConnection.connect();
        } catch (final Throwable t) {
            log.error("Cannot connect to the server URL " + baseURL);
            return true;
        }
        return false;
    }

    @Nullable
    private DynamicTest generateJUnitTest(final ResourceTest resource) {
        try {
            final RestScenario restScenario = JsonUtils.INSTANCE.readValue(resource.open(), RestScenario.class);

            return DynamicTest.dynamicTest(
                    restScenario.getTestName(),
                    () -> {
                        // test code
                        final RestScenarioExecutor restScenarioExecutor = new RestScenarioExecutor(this.serverConfiguration);
                        restScenarioExecutor.addTest(restScenario);
                        restScenarioExecutor.run();
                    });
        } catch (final IOException e) {
            log.error("Cannot read the scenario from {}", resource, e);
        }
        return null;
    }

    /**
     * Instantiates the scanner for the REST tests using the server configuration.
     *
     * @param serverConfiguration the server configuration
     */
    public ScenarioJUnitScanner(final ServerConfiguration serverConfiguration, ResourceTestScanner resourceTestScanner) {
        this.serverConfiguration = serverConfiguration;
        this.resourceTestScanner = resourceTestScanner;
    }
}
