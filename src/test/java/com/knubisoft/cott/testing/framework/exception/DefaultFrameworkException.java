package com.knubisoft.cott.testing.framework.exception;

import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE_WITH_LF;
import static com.knubisoft.cott.testing.framework.util.LogMessage.RETHROWN_ERRORS_LOG;

public class DefaultFrameworkException extends RuntimeException {

    public DefaultFrameworkException() {
    }

    public DefaultFrameworkException(final String message) {
        super(message);
    }

    public DefaultFrameworkException(final List<String> messages) {
        super(String.format(RETHROWN_ERRORS_LOG, String.join(SPACE_WITH_LF, messages)));
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
