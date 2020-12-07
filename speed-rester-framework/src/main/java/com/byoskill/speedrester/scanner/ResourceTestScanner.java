package com.byoskill.speedrester.scanner;

import java.util.stream.Stream;

/**
 * This interface ResourceTestScanner defines the prototype of component in charge to scan the presence of test resources.
 */
public interface ResourceTestScanner {
    /**
     * Finds the tests to be executed.
     *
     * @return the resources.
     */
    Stream<ResourceTest> findTests();
}
