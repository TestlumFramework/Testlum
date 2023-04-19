package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import com.google.common.base.Suppliers;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NOT_DECLARED_WITH_EXECUTOR_FOR_CLASS;


@UtilityClass
public class ExecutorScanner {

    private static final String PACKAGE_TO_SCAN = "com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor";

    private static final Supplier<CommandToExecutorClassMap> CACHE =
            Suppliers.memoize(ExecutorScanner::collectAvailableExecutors);

    private CommandToExecutorClassMap collectAvailableExecutors() {
        CommandToExecutorClassMap map = new CommandToExecutorClassMap();
        for (Class<AbstractUiExecutor<? extends AbstractUiCommand>> executor : scan()) {
            addExecutorToMapIfExists(map, executor);
        }
        return map;
    }

    private void addExecutorToMapIfExists(final CommandToExecutorClassMap map,
                                          final Class<AbstractUiExecutor<? extends AbstractUiCommand>> executor) {
        ExecutorForClass executorForClass = executor.getAnnotation(ExecutorForClass.class);
        if (Objects.isNull(executorForClass)) {
            throw new DefaultFrameworkException(NOT_DECLARED_WITH_EXECUTOR_FOR_CLASS, executor);
        }
        map.put(executorForClass.value(), executor);
    }

    @SuppressWarnings("unchecked")
    private Set<Class<AbstractUiExecutor<? extends AbstractUiCommand>>> scan() {
        return new Reflections(PACKAGE_TO_SCAN).getSubTypesOf(AbstractUiExecutor.class).stream()
                .map(e -> (Class<AbstractUiExecutor<? extends AbstractUiCommand>>) e)
                .filter(e -> !Modifier.isAbstract(e.getModifiers()))
                .collect(Collectors.toSet());
    }

    public CommandToExecutorClassMap getExecutors() {
        return CACHE.get();
    }
}
