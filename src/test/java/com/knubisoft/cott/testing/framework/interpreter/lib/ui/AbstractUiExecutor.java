package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractUiExecutor<T extends AbstractUiCommand> {

    protected final ExecutorDependencies dependencies;

    protected AbstractUiExecutor(final ExecutorDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        if (o.getCondition() == null) {
            execute(o, result);
        } else {
            checkCondition(inject(o.getCondition()), o, result);
        }
    }

    public abstract void execute(T o, CommandResult result);

    protected String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    public void checkCondition(final String original,
                               final T o,
                               final CommandResult result) {
        if (dependencies.getScenarioContext().getCondition(original)) {
            execute(o, result);
        } else {
            log.info("Condition is false");
        }
    }

}
