package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InterpreterProvider} verifying interpreter initialization
 * and lookup for unknown commands.
 */
class InterpreterProviderTest {

    private InterpreterScanner scanner;
    private InterpreterProvider provider;

    @BeforeEach
    void setUp() {
        scanner = mock(InterpreterScanner.class);
        provider = new InterpreterProvider(scanner);
    }

    @Test
    void initCallsScannerGetInterpreters() {
        final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
        when(scanner.getInterpreters()).thenReturn(map);
        provider.init();
        assertNotNull(provider);
    }

    @Test
    void getAppropriateInterpreterThrowsForUnknownCommand() {
        final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
        when(scanner.getInterpreters()).thenReturn(map);
        provider.init();

        final AbstractCommand unknownCommand = mock(AbstractCommand.class);
        assertThrows(DefaultFrameworkException.class,
                () -> provider.getAppropriateInterpreter(unknownCommand,
                        mock(InterpreterDependencies.class)));
    }
}
