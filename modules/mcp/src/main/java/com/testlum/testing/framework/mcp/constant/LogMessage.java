package com.testlum.testing.framework.mcp.constant;

/** Texts the server writes to its log (stderr, never stdout). */
public final class LogMessage {

    public static final String IDLE_EXIT = "idle for {}s, exiting";
    public static final String SESSION_ENDED = "client closed the session";

    private LogMessage() {
    }
}
