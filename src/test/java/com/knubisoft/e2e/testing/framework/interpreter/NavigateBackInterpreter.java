package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.NavigateBack;
import com.knubisoft.e2e.testing.framework.report.CommandResult;

@InterpreterForClass(NavigateBack.class)
public class NavigateBackInterpreter extends AbstractSeleniumInterpreter<NavigateBack> {

    public NavigateBackInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final NavigateBack o, final CommandResult result) {
        dependencies.getWebDriver().navigate().back();
    }
}
