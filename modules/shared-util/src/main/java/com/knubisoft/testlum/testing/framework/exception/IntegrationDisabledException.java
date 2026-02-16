package com.knubisoft.testlum.testing.framework.exception;

public final class IntegrationDisabledException extends DefaultFrameworkException {

    public IntegrationDisabledException(final String format, final Object... args) {
        super(String.format(format, args));
    }

}
