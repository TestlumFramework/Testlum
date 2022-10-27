package com.knubisoft.cott.testing.framework.interpreter.lib.executor;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExecutorHolder { //тут возможно тоже можно поменять навзание

    private final CommandToExecutorClassMap executors = ExecutorScanner.getExecutors();

    public UiCommandExecutor<? extends AbstractCommand> getAppropriateExecutor(AbstractCommand command) {
        UiCommandExecutor<? extends AbstractCommand> executor = executors.get(command.getClass());
        if (executor == null) {
            throw new DefaultFrameworkException();
        }
        return executor;
    }
}
