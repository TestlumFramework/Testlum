package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.AbstractUiCommand;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.FUNCTION_FOR_UI_COMMAND_NOT_FOUND;

@UtilityClass
public class ExecutorProvider {

    private final CommandToExecutorClassMap executors = ExecutorScanner.getExecutors();

    @SneakyThrows
    public AbstractUiExecutor<AbstractUiCommand> getAppropriateExecutor(final AbstractUiCommand command,
                                                                        final ExecutorDependencies dependencies) {
        Class<? extends AbstractUiExecutor<AbstractUiCommand>> executor = executors.get(command.getClass());
        if (executor == null) {
            throw new DefaultFrameworkException(FUNCTION_FOR_UI_COMMAND_NOT_FOUND, command.getClass());
        }
        return executor.getConstructor(ExecutorDependencies.class).newInstance(dependencies);
    }
}
