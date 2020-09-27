package com.byoskill.speedrester;

import com.byoskill.speedrester.execution.RestScenarioExecutor;
import com.byoskill.speedrester.execution.ServerConfiguration;
import com.byoskill.speedrester.model.RestScenario;
import com.byoskill.speedrester.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.DynamicTest;

import javax.annotation.Nullable;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * This scanner will search for scenarios to test your REST API and will produce dynamic tests for JUnit.
 */
@Slf4j
public class ScenarioJUnitScanner {

    private final ServerConfiguration serverConfiguration;

    public ScenarioJUnitScanner(final ServerConfiguration serverConfiguration) {
        this.serverConfiguration = serverConfiguration;
    }

    public List<DynamicTest> scanFolder(final String packageName) throws IOException {
        final String baseURL = this.serverConfiguration.getBaseURL();
        if (baseURL == null) {
            log.error("Tests are disabled, no server url is provided");
            return List.of();
        }
        final URL url = new URL(baseURL);
        try {
            final URLConnection urlConnection = url.openConnection();
            urlConnection.setConnectTimeout(this.serverConfiguration.getDefaultTimeOut());
            urlConnection.connect();
        } catch (final Throwable t) {
            log.error("Cannot connect to the server");
            return List.of();
        }


        return FileUtils.listFiles(new File(packageName), new String[]{"json"}, true).stream()
                .map(
                        resource -> this.generateJUnitTest(resource)
                )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private DynamicTest generateJUnitTest(final File resource) {
        try {
            final RestScenario restScenario = JsonUtils.INSTANCE.readValue(resource, RestScenario.class);
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
}
