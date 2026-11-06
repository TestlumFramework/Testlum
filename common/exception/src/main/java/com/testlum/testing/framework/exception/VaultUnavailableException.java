package com.testlum.testing.framework.exception;

public class VaultUnavailableException extends DefaultFrameworkException implements FormattedFailure {

    public VaultUnavailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    @Override
    public String describe() {
        return getMessage();
    }
}
