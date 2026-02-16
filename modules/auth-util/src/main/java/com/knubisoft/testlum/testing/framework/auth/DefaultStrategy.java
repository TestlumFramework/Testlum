package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;

public class DefaultStrategy extends AbstractAuthStrategy {

    private static final String AUTH_WAS_NOT_DEFINED = "<Auth strategy> was not defined in the <api> configuration. "
            + "Usage example: <auth strategy=\"basic\".../>";

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
