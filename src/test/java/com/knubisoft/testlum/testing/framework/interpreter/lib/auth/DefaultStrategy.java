package com.knubisoft.testlum.testing.framework.interpreter.lib.auth;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.AUTH_WAS_NOT_DEFINED;

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
