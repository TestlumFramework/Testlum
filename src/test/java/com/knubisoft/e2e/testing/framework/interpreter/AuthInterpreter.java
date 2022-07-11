package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.configuration.auth.AuthFactory;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.e2e.testing.framework.util.ResultUtil.API_ALIAS;
import static com.knubisoft.e2e.testing.framework.util.ResultUtil.CREDENTIALS_FILE;

@Slf4j
@InterpreterForClass(Auth.class)
public class AuthInterpreter extends AbstractInterpreter<Auth> {
    private final AuthStrategy authStrategy;

    public AuthInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.authStrategy = AuthFactory.create(dependencies);
    }

    @Override
    protected void acceptImpl(final Auth auth, final CommandResult result) {
        result.put(API_ALIAS, auth.getApiAlias());
        result.put(CREDENTIALS_FILE, auth.getCredentials());
        authStrategy.authenticate(auth, result);
    }

}
