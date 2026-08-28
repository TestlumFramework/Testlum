package com.testlum.testing.connection;

public class IntegrationFailureException extends RuntimeException {

    public IntegrationFailureException(final String message) {
        super(message);
    }

    public IntegrationFailureException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
