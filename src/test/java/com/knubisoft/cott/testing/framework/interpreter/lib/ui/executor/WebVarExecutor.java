package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.VarImplementation;
import com.knubisoft.cott.testing.model.scenario.WebVar;


@ExecutorForClass(WebVar.class)
public class WebVarExecutor extends AbstractUiExecutor<WebVar> {

    public WebVarExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final WebVar webVar, final CommandResult result) {
        VarImplementation varImpl = new VarImplementation(
                dependencies.getScenarioContext(),
                dependencies.getDriver(),
                dependencies.getFile());
        varImpl.setContextVariable(webVar, result);

    }
}
