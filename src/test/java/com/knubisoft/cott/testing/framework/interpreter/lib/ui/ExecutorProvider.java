package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import lombok.experimental.UtilityClass;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.EXECUTOR_FOR_UI_COMMAND_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.MISSING_CONSTRUCTOR;

@UtilityClass
public class ExecutorProvider {

    private final CommandToExecutorClassMap executors = ExecutorScanner.getExecutors();

    @SuppressWarnings("unchecked")
    public AbstractUiExecutor<AbstractUiCommand> getAppropriateExecutor(final AbstractUiCommand command,
                                                                        final ExecutorDependencies dependencies) {
        Class<AbstractUiExecutor<? extends AbstractUiCommand>> executor = executors.get(command.getClass());
        if (executor == null) {
            throw new DefaultFrameworkException(EXECUTOR_FOR_UI_COMMAND_NOT_FOUND, command.getClass());
        }
        try {
            AbstractUiExecutor<? extends AbstractUiCommand> instance =
                    executor.getConstructor(ExecutorDependencies.class).newInstance(dependencies);
            return (AbstractUiExecutor<AbstractUiCommand>) instance;
        } catch (ReflectiveOperationException e) {
            throw new DefaultFrameworkException(MISSING_CONSTRUCTOR, executor, e);
        }
    }
}
