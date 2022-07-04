package com.knubisoft.e2e.testing.framework.interpreter.lib.auth;

import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;

import java.util.Collections;

public class BasicLogout implements LogoutStrategy{
    @Override
    public void logout(InterpreterDependencies dependencies) {
        InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        authorization.setHeaders(Collections.emptyMap());
        dependencies.setAuthorization(authorization);
    }
}
