package com.knubisoft.cott.testing.framework.interpreter.lib.executor;

import com.google.common.base.Suppliers;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;


@UtilityClass
public class ExecutorScanner {

    private static final String PACKAGE_TO_SCAN = "com.knubisoft.cott.testing.framework.interpreter.lib.executor";

    private static final Supplier<CommandToExecutorClassMap> CACHE =
            Suppliers.memoize(ExecutorScanner::collectAvailableExecutors);

    private CommandToExecutorClassMap collectAvailableExecutors() {
        CommandToExecutorClassMap map = new CommandToExecutorClassMap();
        for (Class<? extends UiCommandExecutor<? extends AbstractCommand>> executor : scan()) {
            addExecutorToMapIfExists(map, executor);
        }
        return map;
    }

    @SneakyThrows
    private void addExecutorToMapIfExists(final CommandToExecutorClassMap map,
                                          final Class<? extends UiCommandExecutor<?>> executor) {
        ExecutorForUiCommand executorForUiCommand = executor.getAnnotation(ExecutorForUiCommand.class);
        if (executorForUiCommand == null) {
            throw new DefaultFrameworkException("Error", executor);
        }
        map.put(executorForUiCommand.value(), executor.newInstance());
    }

    @SuppressWarnings("unchecked")
    private Set<Class<? extends UiCommandExecutor<? extends AbstractCommand>>> scan() {
        return new Reflections(PACKAGE_TO_SCAN).getSubTypesOf(UiCommandExecutor.class).stream().
                map(e -> (Class<UiCommandExecutor<? extends AbstractCommand>>) e).
                filter(e -> !Modifier.isAbstract(e.getModifiers())).
                collect(Collectors.toSet());
    }

    public CommandToExecutorClassMap getExecutors() {
        return CACHE.get();
    }
}
