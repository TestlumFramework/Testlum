package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ExecutorProvider {

    private final ExecutorScanner executorScanner;

    private CommandToExecutorClassMap executors;

    @PostConstruct
    public void init() {
        this.executors = executorScanner.getExecutors();
    }

    @SuppressWarnings("unchecked")
    public AbstractUiExecutor<AbstractUiCommand> getAppropriateExecutor(final AbstractUiCommand command,
                                                                        final ExecutorDependencies dependencies) {
        Class<AbstractUiExecutor<? extends AbstractUiCommand>> executor = executors.get(command.getClass());
        if (Objects.isNull(executor)) {
            throw new DefaultFrameworkException(
                    ExceptionMessage.EXECUTOR_FOR_UI_COMMAND_NOT_FOUND, command.getClass());
        }
        try {
            AbstractUiExecutor<? extends AbstractUiCommand> instance =
                    executor.getConstructor(ExecutorDependencies.class).newInstance(dependencies);
            return (AbstractUiExecutor<AbstractUiCommand>) instance;
        } catch (ReflectiveOperationException e) {
            throw new DefaultFrameworkException(ExceptionMessage.MISSING_CONSTRUCTOR, executor, e);
        }
    }
}
