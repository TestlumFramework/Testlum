package com.knubisoft.cott.testing.framework.interpreter;

import com.knubisoft.cott.testing.framework.configuration.auth.AuthFactory;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.interpreter.lib.auth.AuthStrategy;
import com.knubisoft.cott.testing.model.scenario.Logout;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@InterpreterForClass(Logout.class)
public class LogoutInterpreter extends AbstractInterpreter<Logout> {
    private final AuthStrategy authStrategy;

    public LogoutInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        this.authStrategy = AuthFactory.create(dependencies);
    }

    @Override
    protected void acceptImpl(final Logout o, final CommandResult result) {
        authStrategy.logout();
    }
}
