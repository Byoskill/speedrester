package com.byoskill.speedrester.scanner;

import java.io.InputStream;

public interface ResourceTest {

    /**
     * Returns the test filename
     *
     * @return the test filename
     */
    String getTestFilename();

    /**
     * Open the test filename for reading
     *
     * @return the test filename.
     */
    InputStream open();
}
