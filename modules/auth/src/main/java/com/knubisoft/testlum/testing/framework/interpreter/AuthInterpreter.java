package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.auth.AuthFactory;
import com.knubisoft.testlum.testing.framework.auth.AuthStrategy;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Auth.class)
public class AuthInterpreter extends AbstractInterpreter<Auth> {

    private static final String API_ALIAS = "API alias";
    private static final String CREDENTIALS_FILE = "Credentials file";
    private static final String ENDPOINT = "Endpoint";

    public AuthInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Auth o, final CommandResult result) {
        Auth auth = injectCommand(o);
        AuthStrategy authStrategy = AuthFactory.create(dependencies, auth.getApiAlias());
        addAuthMetaData(auth, result);
        authStrategy.authenticate(auth, result);
    }

    private void addAuthMetaData(final Auth auth, final CommandResult result) {
        result.put(API_ALIAS, auth.getApiAlias());
        result.put(ENDPOINT, auth.getLoginEndpoint());
        result.put(CREDENTIALS_FILE, auth.getCredentials());
    }

}
