package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link InterpreterProvider} verifying interpreter initialization,
 * lookup for unknown commands, successful resolution, and reflective instantiation errors.
 */
class InterpreterProviderTest {

    private InterpreterScanner scanner;
    private InterpreterProvider provider;

    @BeforeEach
    void setUp() {
        scanner = mock(InterpreterScanner.class);
        provider = new InterpreterProvider(scanner);
    }

    @Nested
    class Init {

        @Test
        void initCallsScannerGetInterpreters() {
            final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
            when(scanner.getInterpreters()).thenReturn(map);
            provider.init();
            assertNotNull(provider);
            verify(scanner).getInterpreters();
        }

        @Test
        void initCanBeCalledMultipleTimes() {
            final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
            when(scanner.getInterpreters()).thenReturn(map);
            provider.init();
            provider.init();
            verify(scanner, times(2)).getInterpreters();
        }
    }

    @Nested
    class GetAppropriateInterpreter {

        @Test
        void throwsForUnknownCommand() {
            final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
            when(scanner.getInterpreters()).thenReturn(map);
            provider.init();

            final AbstractCommand unknownCommand = mock(AbstractCommand.class);
            assertThrows(DefaultFrameworkException.class,
                    () -> provider.getAppropriateInterpreter(unknownCommand,
                            mock(InterpreterDependencies.class)));
        }

        @Test
        void throwsForNullMappedInterpreter() {
            final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
            when(scanner.getInterpreters()).thenReturn(map);
            provider.init();

            final AbstractCommand command = mock(AbstractCommand.class);
            DefaultFrameworkException ex = assertThrows(DefaultFrameworkException.class,
                    () -> provider.getAppropriateInterpreter(command, mock(InterpreterDependencies.class)));
            assertNotNull(ex.getMessage());
        }

        @SuppressWarnings("unchecked")
        @Test
        void throwsWhenInterpreterClassLacksConstructor() {
            final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();

            // Use a class that does not have a constructor taking InterpreterDependencies
            Class<AbstractInterpreter<? extends AbstractCommand>> badClass =
                    (Class<AbstractInterpreter<? extends AbstractCommand>>) (Class<?>) BadInterpreter.class;

            // Create a concrete AbstractCommand subclass for the test
            Class<? extends AbstractCommand> cmdClass = TestCommand.class;
            map.put(cmdClass, badClass);

            when(scanner.getInterpreters()).thenReturn(map);
            provider.init();

            TestCommand command = new TestCommand();
            assertThrows(DefaultFrameworkException.class,
                    () -> provider.getAppropriateInterpreter(command, mock(InterpreterDependencies.class)));
        }

        @SuppressWarnings("unchecked")
        @Test
        void returnsInterpreterWhenCorrectlyMapped() {
            final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();

            Class<AbstractInterpreter<? extends AbstractCommand>> goodClass =
                    (Class<AbstractInterpreter<? extends AbstractCommand>>) (Class<?>) GoodInterpreter.class;

            Class<? extends AbstractCommand> cmdClass = TestCommand.class;
            map.put(cmdClass, goodClass);

            when(scanner.getInterpreters()).thenReturn(map);
            provider.init();

            TestCommand command = new TestCommand();
            InterpreterDependencies deps = createMockDependencies();
            AbstractInterpreter<AbstractCommand> result =
                    provider.getAppropriateInterpreter(command, deps);

            assertNotNull(result);
            assertInstanceOf(GoodInterpreter.class, result);
        }
    }

    private InterpreterDependencies createMockDependencies() {
        InterpreterDependencies deps = mock(InterpreterDependencies.class);
        ApplicationContext ctx = mock(ApplicationContext.class);
        when(deps.getContext()).thenReturn(ctx);
        when(ctx.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(ctx.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(ctx.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(ctx.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(ctx.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        GlobalTestConfiguration config = mock(GlobalTestConfiguration.class);
        when(config.isStopScenarioOnFailure()).thenReturn(false);
        when(ctx.getBean(GlobalTestConfiguration.class)).thenReturn(config);
        return deps;
    }

    // Test helpers

    public static class TestCommand extends AbstractCommand {
    }

    /**
     * An interpreter without the required InterpreterDependencies constructor.
     */
    @InterpreterForClass(TestCommand.class)
    public static class BadInterpreter extends AbstractInterpreter<TestCommand> {
        // No InterpreterDependencies constructor
        BadInterpreter() {
            super(null);
        }

        @Override
        protected void acceptImpl(final TestCommand command, final CommandResult result) {
        }
    }

    /**
     * An interpreter with the required InterpreterDependencies constructor.
     */
    @InterpreterForClass(TestCommand.class)
    public static class GoodInterpreter extends AbstractInterpreter<TestCommand> {
        public GoodInterpreter(final InterpreterDependencies dependencies) {
            super(dependencies);
        }

        @Override
        protected void acceptImpl(final TestCommand command, final CommandResult result) {
        }
    }
}
