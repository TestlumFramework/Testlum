package com.knubisoft.testlum.testing.framework.exception;

import java.util.List;

public class DefaultFrameworkException extends RuntimeException {

    private static final String SPACE_WITH_LF = " \n";
    private static final String RETHROWN_ERRORS_TEMPLATE = "Errors:%n%s";

    public DefaultFrameworkException() {
    }

    public DefaultFrameworkException(final String message) {
        super(message);
    }

    public DefaultFrameworkException(final List<String> messages) {
        super(String.format(RETHROWN_ERRORS_TEMPLATE, String.join(SPACE_WITH_LF, messages)));
    }

    public DefaultFrameworkException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    public DefaultFrameworkException(final Throwable cause) {
        super(cause);
    }

    public DefaultFrameworkException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
