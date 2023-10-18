package com.knubisoft.testlum.testing.framework.exception;

import java.io.File;

public class FileLinkingException extends RuntimeException {

    public static final String FOLDER_LOCATION_ERROR_MESSAGE = "%s. Expected location -> %s";

    private static final String UNABLE_FIND_FILE_IN_ROOT_FOLDER = "Unable to find file by key [%1$s] "
            + "Initial scan folder [%2$s] with strategy recursive walk to root folder [%3$s]";

    public FileLinkingException(final File start,
                                final File root,
                                final String fileKey) {
        super(String.format(UNABLE_FIND_FILE_IN_ROOT_FOLDER, fileKey, start, root));
    }

    public FileLinkingException(final String errorMessage, final File start) {
        super(String.format(FOLDER_LOCATION_ERROR_MESSAGE, errorMessage, start.getAbsolutePath()));
    }
}
