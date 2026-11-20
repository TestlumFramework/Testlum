package com.testlum.testing.framework.mcp.exception;

/** Thrown by tool handlers for problems the caller can fix; reported to the client as a tool error. */
public class ToolException extends RuntimeException {

    public ToolException(final String message) {
        super(message);
    }

    public ToolException(final String format, final Object... args) {
        super(String.format(format, args));
    }

    public ToolException(final Throwable cause, final String format, final Object... args) {
        super(String.format(format, args), cause);
    }
}
