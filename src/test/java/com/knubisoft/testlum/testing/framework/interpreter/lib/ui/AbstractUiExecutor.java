package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.InjectionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;

import static java.util.Objects.nonNull;

public abstract class AbstractUiExecutor<T extends AbstractUiCommand> {

    protected final ExecutorDependencies dependencies;

    protected AbstractUiExecutor(final ExecutorDependencies dependencies) {
        this.dependencies = dependencies;
    }

    public final void apply(final T o, final CommandResult result) {
        result.setComment(o.getComment());
        LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), o);
        if (ConditionUtil.isTrue(o.getCondition(), dependencies.getScenarioContext(), result)) {
            execute(o, result);
        }
    }

    protected abstract void execute(T o, CommandResult result);

    protected String inject(final String original) {
        return dependencies.getScenarioContext().inject(original);
    }

    protected <Y> Y injectCommand(final Y o, final Class<Y> clazz) {
        if (nonNull(o)) {
            return InjectionUtil.injectObject(o, clazz, dependencies.getScenarioContext());
        }
        return null;
    }
}
