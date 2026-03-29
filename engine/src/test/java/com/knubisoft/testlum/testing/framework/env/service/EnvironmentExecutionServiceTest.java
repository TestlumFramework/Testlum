package com.knubisoft.testlum.testing.framework.env.service;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

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

    @Test
    void constructorCreatesExecutorsForEachEnvironment() {
        Environment env1 = createEnvironment("env1", 2);
        Environment env2 = createEnvironment("env2", 3);

        service = new EnvironmentExecutionService(List.of(env1, env2));

        AtomicBoolean executed = new AtomicBoolean(false);
        assertDoesNotThrow(() -> service.runInEnvironment("env1", () -> executed.set(true)));
        assertTrue(executed.get());
    }

    @Test
    void runInEnvironmentExecutesTaskSuccessfully() {
        Environment env = createEnvironment("testEnv", 1);
        service = new EnvironmentExecutionService(List.of(env));

        AtomicBoolean taskExecuted = new AtomicBoolean(false);
        service.runInEnvironment("testEnv", () -> taskExecuted.set(true));

        assertTrue(taskExecuted.get());
    }

    @Test
    void runInEnvironmentThrowsForUnknownEnvironment() {
        Environment env = createEnvironment("knownEnv", 1);
        service = new EnvironmentExecutionService(List.of(env));

        assertThrows(DefaultFrameworkException.class,
                () -> service.runInEnvironment("unknownEnv", () -> { }));
    }

    @Test
    void shutdownCompletesWithoutError() {
        Environment env = createEnvironment("shutdownEnv", 1);
        service = new EnvironmentExecutionService(List.of(env));

        assertDoesNotThrow(() -> service.shutdown());
        service = null;
    }

    private Environment createEnvironment(final String folder, final int threads) {
        Environment env = mock(Environment.class);
        when(env.getFolder()).thenReturn(folder);
        when(env.getThreads()).thenReturn(threads);
        return env;
    }
}
