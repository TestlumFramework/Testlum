package com.testlum.testing.framework.exception;

public class IncorrectCryptographyValueException extends RuntimeException {
    public IncorrectCryptographyValueException(final String message) {
        super(message);
    }
}
