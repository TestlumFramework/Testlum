package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.auth.AuthorizationConstant.HEADER_AUTHORIZATION;

@RequiredArgsConstructor
public abstract class AbstractAuthStrategy implements AuthStrategy {

    protected final InterpreterDependencies dependencies;

    protected void login(final String token, final String prefix) {
        final InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        final Map<String, String> headers = new HashMap<>();
        headers.put(HEADER_AUTHORIZATION, prefix + token);
        authorization.setHeaders(headers);
        dependencies.setAuthorization(authorization);
    }

    @Override
    public void logout() {
        InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        authorization.setHeaders(Collections.emptyMap());
        dependencies.setAuthorization(authorization);
    }
}
