package com.knubisoft.cott.testing.framework.interpreter.lib;

import com.google.common.base.Suppliers;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;
import org.reflections.Reflections;

import java.lang.reflect.Modifier;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.knubisoft.cott.testing.framework.util.LogMessage.NOT_DECLARED_WITH_INTERPRETER_FOR_CLASS;

@UtilityClass
public class InterpreterScanner {

    private static final String PACKAGE_TO_SCAN = "com.knubisoft.cott.testing.framework.interpreter";

    private static final Supplier<CommandToInterpreterClassMap> CACHE =
            Suppliers.memoize(InterpreterScanner::collectAvailableInterpreters);

    private CommandToInterpreterClassMap collectAvailableInterpreters() {
        CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
        for (Class<AbstractInterpreter<? extends AbstractCommand>> interpreter : scan()) {
            addInterpreterToMapIfExists(map, interpreter);
        }
        return map;
    }

    private void addInterpreterToMapIfExists(final CommandToInterpreterClassMap map,
                                             final Class<AbstractInterpreter<? extends AbstractCommand>> interpreter) {
        InterpreterForClass interpreterForClass = interpreter.getAnnotation(InterpreterForClass.class);
        if (interpreterForClass == null) {
            throw new DefaultFrameworkException(NOT_DECLARED_WITH_INTERPRETER_FOR_CLASS, interpreter);
        }
        map.put(interpreterForClass.value(), interpreter);
    }

    @SuppressWarnings("unchecked")
    private Set<Class<AbstractInterpreter<? extends AbstractCommand>>> scan() {
        return new Reflections(PACKAGE_TO_SCAN).getSubTypesOf(AbstractInterpreter.class).stream().
                map(e -> (Class<AbstractInterpreter<? extends AbstractCommand>>) e).
                filter(e -> !Modifier.isAbstract(e.getModifiers())).
                collect(Collectors.toSet());
    }

    public CommandToInterpreterClassMap getInterpreters() {
        return CACHE.get();
    }
}
