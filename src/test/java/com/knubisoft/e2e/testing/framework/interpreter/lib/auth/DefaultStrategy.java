package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Auth;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.AUTH_WAS_NOT_DEFINED;

public class DefaultStrategy implements AuthStrategy {
    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        throw new DefaultFrameworkException(AUTH_WAS_NOT_DEFINED);
    }

    @Override
    public void logout() {
        throw new DefaultFrameworkException(AUTH_WAS_NOT_DEFINED);
    }
}
