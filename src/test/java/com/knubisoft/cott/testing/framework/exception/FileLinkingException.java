package com.knubisoft.cott.testing.framework.exception;

import com.knubisoft.cott.testing.framework.util.LogMessage;

import java.io.File;

public class FileLinkingException extends RuntimeException {

    public FileLinkingException(final File start,
                                final File root,
                                final String fileKey) {
        super(String.format(LogMessage.UNABLE_FIND_FILE_IN_ROOT_FOLDER, fileKey, start, root));
    }
}
