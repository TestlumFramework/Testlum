package com.knubisoft.cott.testing.framework.exception;

import com.knubisoft.cott.testing.framework.constant.ExceptionMessage;

import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE_WITH_LF;

public class DefaultFrameworkException extends RuntimeException {

    public DefaultFrameworkException() {
    }

    public DefaultFrameworkException(final String message) {
        super(message);
    }

    public DefaultFrameworkException(final List<String> messages) {
        super(String.format(ExceptionMessage.RETHROWN_ERRORS_TEMPLATE, String.join(SPACE_WITH_LF, messages)));
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
