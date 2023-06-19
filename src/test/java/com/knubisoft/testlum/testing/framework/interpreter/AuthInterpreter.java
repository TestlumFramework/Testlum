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

@Slf4j
@InterpreterForClass(Auth.class)
public class AuthInterpreter extends AbstractInterpreter<Auth> {

    public AuthInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Auth o, final CommandResult result) {
        Auth auth = injectCommand(o);
        AuthStrategy authStrategy = AuthFactory.create(dependencies, auth.getApiAlias());
        ResultUtil.addAuthMetaData(auth, result);
        authStrategy.authenticate(auth, result);
    }

}
