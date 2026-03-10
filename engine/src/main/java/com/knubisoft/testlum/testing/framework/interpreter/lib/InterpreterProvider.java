package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.INTERPRETER_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.MISSING_CONSTRUCTOR;

@RequiredArgsConstructor
@Service
public class InterpreterProvider {

    private final InterpreterScanner scanner;

    private CommandToInterpreterClassMap interpreters;

    @PostConstruct
    public void init() {
        this.interpreters = scanner.getInterpreters();
    }

    @SuppressWarnings("unchecked")
    public AbstractInterpreter<AbstractCommand> getAppropriateInterpreter(final AbstractCommand command,
                                                                          final InterpreterDependencies dependencies) {
        Class<AbstractInterpreter<? extends AbstractCommand>> interpreter = interpreters.get(command.getClass());
        if (Objects.isNull(interpreter)) {
            throw new DefaultFrameworkException(INTERPRETER_NOT_FOUND, command.getClass());
        }
        try {
            AbstractInterpreter<? extends AbstractCommand> instance =
                    interpreter.getConstructor(InterpreterDependencies.class).newInstance(dependencies);
            return (AbstractInterpreter<AbstractCommand>) instance;
        } catch (ReflectiveOperationException e) {
            throw new DefaultFrameworkException(MISSING_CONSTRUCTOR, interpreter, e);
        }
    }
}
