package com.knubisoft.testlum.testing.framework.exception;

public interface FormattedFailure {

    static FormattedFailure findIn(final Throwable failure) {
        Throwable current = failure;
        while (current != null) {
            if (current instanceof FormattedFailure formatted) {
                return formatted;
            }
            if (current.getCause() == current) {
                return null;
            }
            current = current.getCause();
        }
        return null;
    }

    String describe();

    default boolean isRetryable() {
        return false;
    }
}
