package com.knubisoft.cott.testing.framework.exception;

public class AbsentConnectionException extends RuntimeException {
    public AbsentConnectionException(final String message) {
        super(message);
    }
}
