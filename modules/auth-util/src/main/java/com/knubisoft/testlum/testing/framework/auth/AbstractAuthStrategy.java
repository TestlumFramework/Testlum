package com.knubisoft.testlum.testing.framework.auth;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractAuthStrategy implements AuthStrategy {

    protected static final String ALIAS_LOG = LogFormat.table("Alias");
    protected static final String ENDPOINT_LOG = LogFormat.table("Endpoint");
    protected static final String CREDENTIALS_LOG = LogFormat.table("Credentials");
    protected static final String AUTHENTICATION_TYPE = "Authentication type";

    protected final InterpreterDependencies dependencies;

    protected void login(final String token, final String prefix) {
        final InterpreterDependencies.Authorization authorization = new InterpreterDependencies.Authorization();
        final Map<String, String> headers = new HashMap<>();
        headers.put(AuthorizationConstant.HEADER_AUTHORIZATION, prefix + token);
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
