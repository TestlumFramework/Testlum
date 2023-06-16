package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.configuration.auth.AuthFactory;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Auth;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.testlum.testing.framework.util.ResultUtil.API_ALIAS;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CREDENTIALS_FILE;

@Slf4j
@InterpreterForClass(Auth.class)
public class AuthInterpreter extends AbstractInterpreter<Auth> {

    public AuthInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Auth auth, final CommandResult result) {
        AuthStrategy authStrategy = AuthFactory.create(dependencies, auth.getApiAlias());
        result.put(API_ALIAS, auth.getApiAlias());
        ResultUtil.addAuthMetaData(auth, result);
        result.put(CREDENTIALS_FILE, auth.getCredentials());
        authStrategy.authenticate(auth, result);
    }

}
