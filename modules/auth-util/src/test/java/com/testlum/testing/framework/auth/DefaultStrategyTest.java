package com.testlum.testing.framework.auth;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.model.scenario.Auth;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class DefaultStrategyTest {

    private DefaultStrategy strategy;

    @BeforeEach
    void setUp() {
        InterpreterDependencies dependencies = mock(InterpreterDependencies.class);
        strategy = new DefaultStrategy(dependencies);
    }

    @Test
    void authenticateThrowsWithDescriptiveMessage() {
        Auth auth = new Auth();
        CommandResult result = new CommandResult();

        DefaultFrameworkException exception = assertThrows(
                DefaultFrameworkException.class,
                () -> strategy.authenticate(auth, result));

        assertTrue(exception.getMessage().contains("Auth strategy"));
    }

    @Test
    void logoutThrowsWithDescriptiveMessage() {
        DefaultFrameworkException exception = assertThrows(
                DefaultFrameworkException.class,
                () -> strategy.logout());

        assertTrue(exception.getMessage().contains("Auth strategy"));
    }
}
