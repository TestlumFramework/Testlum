package com.knubisoft.testlum.testing.framework.env.service;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EnvironmentExecutionServiceTest {

    private EnvironmentExecutionService service;

    @AfterEach
    void tearDown() {
        if (service != null) {
            assertDoesNotThrow(() -> service.shutdown());
        }
    }

    @Nested
    class Constructor {

        @Test
        void createsExecutorsForEachEnvironment() {
            Environment env1 = createEnvironment("env1", 2);
            Environment env2 = createEnvironment("env2", 3);

            service = new EnvironmentExecutionService(List.of(env1, env2));

            AtomicBoolean executed = new AtomicBoolean(false);
            assertDoesNotThrow(() -> service.runInEnvironment("env1", () -> executed.set(true)));
            assertTrue(executed.get());
        }

        @Test
        void handlesZeroThreadsGracefully() {
            Environment env = createEnvironment("zeroEnv", 0);
            service = new EnvironmentExecutionService(List.of(env));

            AtomicBoolean executed = new AtomicBoolean(false);
            service.runInEnvironment("zeroEnv", () -> executed.set(true));
            assertTrue(executed.get());
        }

        @Test
        void handlesNegativeThreadsGracefully() {
            Environment env = createEnvironment("negEnv", -5);
            service = new EnvironmentExecutionService(List.of(env));

            AtomicBoolean executed = new AtomicBoolean(false);
            service.runInEnvironment("negEnv", () -> executed.set(true));
            assertTrue(executed.get());
        }
    }

    @Nested
    class RunInEnvironment {

        @Test
        void executesTaskSuccessfully() {
            Environment env = createEnvironment("testEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            AtomicBoolean taskExecuted = new AtomicBoolean(false);
            service.runInEnvironment("testEnv", () -> taskExecuted.set(true));

            assertTrue(taskExecuted.get());
        }

        @Test
        void throwsForUnknownEnvironment() {
            Environment env = createEnvironment("knownEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            assertThrows(DefaultFrameworkException.class,
                    () -> service.runInEnvironment("unknownEnv", () -> { }));
        }

        @Test
        void propagatesRuntimeExceptionFromTask() {
            Environment env = createEnvironment("errEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            assertThrows(DefaultFrameworkException.class,
                    () -> service.runInEnvironment("errEnv", () -> {
                        throw new RuntimeException("task failed");
                    }));
        }

        @Test
        void propagatesDefaultFrameworkExceptionFromTask() {
            Environment env = createEnvironment("fwEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            assertThrows(DefaultFrameworkException.class,
                    () -> service.runInEnvironment("fwEnv", () -> {
                        throw new DefaultFrameworkException("framework error");
                    }));
        }

        @Test
        void runsMultipleTasksSequentially() {
            Environment env = createEnvironment("seqEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            AtomicReference<String> result = new AtomicReference<>("");
            service.runInEnvironment("seqEnv", () -> result.set(result.get() + "A"));
            service.runInEnvironment("seqEnv", () -> result.set(result.get() + "B"));

            assertEquals("AB", result.get());
        }
    }

    @Nested
    class Shutdown {

        @Test
        void shutdownCompletesWithoutError() {
            Environment env = createEnvironment("shutdownEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            assertDoesNotThrow(() -> service.shutdown());
            service = null;
        }

        @Test
        void shutdownMultipleEnvironments() {
            Environment env1 = createEnvironment("env1", 1);
            Environment env2 = createEnvironment("env2", 2);
            service = new EnvironmentExecutionService(List.of(env1, env2));

            assertDoesNotThrow(() -> service.shutdown());
            service = null;
        }

        @Test
        void doubleShutdownDoesNotThrow() {
            Environment env = createEnvironment("dblEnv", 1);
            service = new EnvironmentExecutionService(List.of(env));

            service.shutdown();
            assertDoesNotThrow(() -> service.shutdown());
            service = null;
        }
    }

    private Environment createEnvironment(final String folder, final int threads) {
        Environment env = mock(Environment.class);
        when(env.getFolder()).thenReturn(folder);
        when(env.getThreads()).thenReturn(threads);
        return env;
    }
}
