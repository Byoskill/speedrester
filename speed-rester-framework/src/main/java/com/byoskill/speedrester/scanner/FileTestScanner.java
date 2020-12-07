package com.byoskill.speedrester.scanner;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

/**
 * This class implements the resource test scanner that scans a folder for JSON Test files.
 */
@Slf4j
public class FileTestScanner implements ResourceTestScanner {
    private Path testFolder;

    /**
     * Initializes the file TestScanner
     *
     * @param testFolder the test folder
     */
    public FileTestScanner(final Path testFolder) {

        this.testFolder = testFolder;
    }

    /**
     * Finds the tests in the given folder; recursively.
     *
     * @return the list of tests
     */
    @Override
    public Stream<ResourceTest> findTests() {
        if (!Files.isDirectory(this.testFolder)) {
            log.error("Cannot find any test file, the path {} is not a folder", this.testFolder);
            return Stream.empty();
        }
        return FileUtils.listFiles(testFolder.toFile(), new String[]{
                "json"
        }, true).stream()
                .map(new FileResourceTest());
    }
}
