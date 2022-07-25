package com.knubisoft.cott.testing.framework.interpreter.lib.auth;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.Auth;

import static com.knubisoft.cott.testing.framework.util.LogMessage.AUTH_WAS_NOT_DEFINED;

public class DefaultStrategy extends AbstractAuthStrategy {

    public DefaultStrategy(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void authenticate(final Auth auth, final CommandResult result) {
        throw new DefaultFrameworkException(AUTH_WAS_NOT_DEFINED);
    }

    @Override
    public void logout() {
        throw new DefaultFrameworkException(AUTH_WAS_NOT_DEFINED);
    }
}
