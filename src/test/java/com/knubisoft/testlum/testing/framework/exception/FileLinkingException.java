package com.knubisoft.testlum.testing.framework.exception;

import java.io.File;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNABLE_FIND_FILE_IN_ROOT_FOLDER;

public class FileLinkingException extends RuntimeException {

    public FileLinkingException(final File start,
                                final File root,
                                final String fileKey) {
        super(String.format(UNABLE_FIND_FILE_IN_ROOT_FOLDER, fileKey, start, root));
    }
}
