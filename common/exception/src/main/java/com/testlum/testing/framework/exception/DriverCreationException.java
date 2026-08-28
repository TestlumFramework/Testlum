package com.testlum.testing.framework.exception;

public class DriverCreationException extends RuntimeException implements FormattedFailure {

    private final boolean retryable;

    public DriverCreationException(final String description, final boolean retryable, final Throwable cause) {
        super(description, cause);
        this.retryable = retryable;
    }

    @Override
    public String describe() {
        return getMessage();
    }

    @Override
    public boolean isRetryable() {
        return retryable;
    }
}
