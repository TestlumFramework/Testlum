package com.testlum.comparator.exception;

public class MatchException extends RuntimeException {

    private final String detail;
    private String path = "";

    public MatchException(final String message) {
        super(message);
        this.detail = message;
    }

    /** Adds a path segment in front of the ones already recorded, so parents can name where a mismatch is. */
    public void prependPath(final String segment) {
        this.path = segment + path;
    }

    @Override
    public String getMessage() {
        return path.isEmpty() ? detail : path + ": " + detail;
    }
}
