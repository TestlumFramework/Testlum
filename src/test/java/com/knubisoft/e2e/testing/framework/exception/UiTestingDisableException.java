package com.knubisoft.e2e.testing.framework.exception;

public class UiTestingDisableException extends RuntimeException {

    private static final String ERROR_MESSAGE = "UI testing was disabled or you didn't provide UI configuration in "
            + "global-config file. All scenarios contain UI testing steps or you don't have any backend scenarios.";

    public UiTestingDisableException() {
        super(ERROR_MESSAGE);
    }
}
