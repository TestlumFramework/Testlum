package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.configuration.auth.AuthFactory;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Logout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Logout.class)
public class LogoutInterpreter extends AbstractInterpreter<Logout> {

    public LogoutInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Logout o, final CommandResult result) {
        Logout logout = injectCommand(o);
        AuthStrategy authStrategy = AuthFactory.create(dependencies, logout.getAlias());
        authStrategy.logout();
    }
}
