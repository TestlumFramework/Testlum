package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;

public abstract class AbstractUiExecutor<T extends AbstractUiCommand> {

    protected final ExecutorDependencies dependencies;

    protected AbstractUiExecutor(final ExecutorDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        if (ConditionUtil.isTrue(o.getCondition(), dependencies.getScenarioContext(), result)) {
            execute(o, result);
        }
    }

    public abstract void execute(T o, CommandResult result);

    protected String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

}
