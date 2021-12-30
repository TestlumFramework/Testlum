package com.knubisoft.e2e.testing.framework.exception;

import com.knubisoft.e2e.testing.framework.util.LogMessage;

import java.io.File;

public class FileLinkingException extends RuntimeException {

    public FileLinkingException(final File start,
                                final File root,
                                final String fileKey) {
        super(String.format(LogMessage.UNABLE_FIND_FILE_IN_ROOT_FOLDER, fileKey, start, root));
    }
}
