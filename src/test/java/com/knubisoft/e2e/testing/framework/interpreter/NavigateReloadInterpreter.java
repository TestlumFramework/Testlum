package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.NavigateReload;
import com.knubisoft.e2e.testing.framework.report.CommandResult;

@InterpreterForClass(NavigateReload.class)
public class NavigateReloadInterpreter extends AbstractSeleniumInterpreter<NavigateReload> {

    public NavigateReloadInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final NavigateReload o, final CommandResult result) {
        dependencies.getWebDriver().navigate().refresh();
    }
}
