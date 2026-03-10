package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.auth.AuthFactory;
import com.knubisoft.testlum.testing.framework.auth.AuthStrategy;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Logout;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Logout.class)
public class LogoutInterpreter extends AbstractInterpreter<Logout> {

    private final AuthFactory authFactory;

    public LogoutInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.authFactory = dependencies.getContext().getBean(AuthFactory.class);
    }

    @Override
    protected void acceptImpl(final Logout o, final CommandResult result) {
        Logout logout = injectCommand(o);
        AuthStrategy authStrategy = authFactory.create(dependencies, logout.getAlias());
        authStrategy.logout();
    }
}
