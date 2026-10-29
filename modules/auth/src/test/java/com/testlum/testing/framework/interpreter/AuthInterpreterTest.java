package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.auth.AuthFactory;
import com.testlum.testing.framework.auth.AuthStrategy;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.exception.StopSignalException;
import com.testlum.testing.framework.interpreter.lib.CommandRunner;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Auth;
import com.testlum.testing.model.scenario.Http;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class AuthInterpreterTest {

    private AuthInterpreter interpreter;
    private AuthFactory authFactory;
    private AuthStrategy authStrategy;
    private ConditionProvider conditionProvider;
    private CommandRunner commandRunner;
    private InterpreterDependencies dependencies;
    private InterpreterDependencies.Authorization authorization;

    @BeforeEach
    void setUp() {
        authFactory = mock(AuthFactory.class);
        authStrategy = mock(AuthStrategy.class);
        conditionProvider = mock(ConditionProvider.class);
        commandRunner = mock(CommandRunner.class);
        ApplicationContext context = mockContext();
        dependencies = InterpreterDependencies.builder().context(context).file(new File("test.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>())).position(new AtomicInteger(1))
                .environment("test").build();
        authorization = new InterpreterDependencies.Authorization(Map.of("Authorization", "Bearer token"));
        interpreter = new AuthInterpreter(dependencies);
    }

    private ApplicationContext mockContext() {
        ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(AuthFactory.class)).thenReturn(authFactory);
        when(context.getBean(CommandRunner.class)).thenReturn(commandRunner);
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(new GlobalTestConfiguration());
        return context;
    }

    @Test
    void authenticatesRunsOriginalChildrenAndSetsMetadata() {
        Auth auth = command(true);
        Http child = new Http();
        child.setAlias("{{aliasCreatedByPreviousCommand}}");
        auth.getCommands().add(child);
        stubAuthentication("JWT");
        CommandResult result = new CommandResult();
        doAnswer(call -> {
            assertSame(authorization, dependencies.getAuthorization());
            assertSame(auth.getCommands(), call.getArgument(0));
            assertEquals("{{aliasCreatedByPreviousCommand}}", child.getAlias());
            return null;
        }).when(commandRunner).runCommands(anyList(), any(), any(), anyList());

        interpreter.acceptImpl(auth, result);

        verify(authStrategy).logout();
        assertNull(dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
        assertEquals("JWT", result.getMetadata().get("Auth strategy alias"));
        assertEquals("creds.json", result.getMetadata().get("Credentials file"));
        assertEquals(true, result.getMetadata().get("Auto logout"));
    }

    @Test
    void appliesDefaultAliasWhenNotSpecified() {
        Auth auth = command(true);
        auth.setStrategyAlias(null);
        stubAuthentication("DEFAULT");
        interpreter.acceptImpl(auth, new CommandResult());
        verify(authFactory).create(dependencies, "DEFAULT");
    }

    @Test
    void injectsAliasAndCredentialsWithoutCopyingChildren() {
        dependencies.getScenarioContext().set("alias", "JWT");
        dependencies.getScenarioContext().set("credentials", "creds.json");
        Auth auth = command(false);
        auth.setStrategyAlias("{{alias}}");
        auth.setCredentials("{{credentials}}");
        stubAuthentication("JWT");
        interpreter.acceptImpl(auth, new CommandResult());
        ArgumentCaptor<Auth> prepared = ArgumentCaptor.forClass(Auth.class);
        verify(authStrategy).authenticate(prepared.capture(), any());
        assertEquals("JWT", prepared.getValue().getStrategyAlias());
        assertEquals("creds.json", prepared.getValue().getCredentials());
    }

    @Test
    void retainsAuthorizationWhenAutoLogoutDisabled() {
        stubAuthentication("JWT");
        interpreter.acceptImpl(command(false), new CommandResult());
        assertSame(authorization, dependencies.getAuthorization());
        verify(authStrategy, never()).logout();
        assertFalse(dependencies.isAuthBlockActive());
    }

    @Test
    void failedLoginSkipsChildrenAndClearsPreviousUser() {
        dependencies.setAuthorization(authorization);
        when(authFactory.create(dependencies, "JWT")).thenReturn(authStrategy);
        doThrow(new IllegalStateException("Login failed")).when(authStrategy).authenticate(any(), any());
        assertThrows(IllegalStateException.class,
                () -> interpreter.acceptImpl(command(false), new CommandResult()));
        verifyNoInteractions(commandRunner);
        assertNull(dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
    }

    @Test
    void failedFactoryLookupClearsPreviousUserAndResetsBlockFlag() {
        dependencies.setAuthorization(authorization);
        when(authFactory.create(dependencies, "JWT")).thenThrow(new DefaultFrameworkException("Missing strategy"));
        assertThrows(DefaultFrameworkException.class,
                () -> interpreter.acceptImpl(command(true), new CommandResult()));
        assertNull(dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
        verifyNoInteractions(commandRunner);
    }

    @Test
    void falseConditionSkipsEntireBlockAndPreservesPreviousUser() {
        dependencies.setAuthorization(authorization);
        Auth auth = command(true);
        auth.setCondition("false");
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(false);
        interpreter.apply(auth, new CommandResult());
        verifyNoInteractions(authFactory, authStrategy, commandRunner);
        assertSame(authorization, dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
    }

    @Test
    void trueConditionRunsEntireBlock() {
        stubAuthentication("JWT");
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);
        interpreter.apply(command(true), new CommandResult());
        verify(commandRunner).runCommands(anyList(), eq(dependencies), any(), anyList());
        verify(authStrategy).logout();
    }

    @Test
    void logsOutWhenChildrenStopScenario() {
        stubAuthentication("JWT");
        doThrow(new StopSignalException()).when(commandRunner).runCommands(anyList(), any(), any(), anyList());
        assertThrows(StopSignalException.class,
                () -> interpreter.acceptImpl(command(true), new CommandResult()));
        verify(authStrategy).logout();
        assertNull(dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
    }

    @Test
    void retainsAuthorizationOnChildFailureWhenAutoLogoutDisabled() {
        stubAuthentication("JWT");
        doThrow(new IllegalStateException("Request failed"))
                .when(commandRunner).runCommands(anyList(), any(), any(), anyList());
        assertThrows(IllegalStateException.class,
                () -> interpreter.acceptImpl(command(false), new CommandResult()));
        assertSame(authorization, dependencies.getAuthorization());
        verify(authStrategy, never()).logout();
    }

    @Test
    void logoutFailureDoesNotHideOriginalFailure() {
        stubAuthentication("JWT");
        IllegalStateException original = new IllegalStateException("Request failed");
        IllegalArgumentException cleanup = new IllegalArgumentException("Logout failed");
        doThrow(original).when(commandRunner).runCommands(anyList(), any(), any(), anyList());
        doThrow(cleanup).when(authStrategy).logout();
        Throwable actual = assertThrows(IllegalStateException.class,
                () -> interpreter.acceptImpl(command(true), new CommandResult()));
        assertSame(original, actual);
        assertArrayEquals(new Throwable[]{cleanup}, actual.getSuppressed());
        assertNull(dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
    }

    @Test
    void reportsLogoutFailureAfterSuccessfulCommands() {
        stubAuthentication("JWT");
        doThrow(new IllegalStateException("Logout failed")).when(authStrategy).logout();
        assertThrows(IllegalStateException.class,
                () -> interpreter.acceptImpl(command(true), new CommandResult()));
        assertNull(dependencies.getAuthorization());
        assertFalse(dependencies.isAuthBlockActive());
    }

    @Test
    void logoutFailureDoesNotHideOriginalError() {
        stubAuthentication("JWT");
        AssertionError original = new AssertionError("Request failed");
        doThrow(original).when(commandRunner).runCommands(anyList(), any(), any(), anyList());
        doThrow(new IllegalStateException("Logout failed")).when(authStrategy).logout();
        Throwable actual = assertThrows(AssertionError.class,
                () -> interpreter.acceptImpl(command(true), new CommandResult()));
        assertSame(original, actual);
        assertEquals(1, actual.getSuppressed().length);
        assertNull(dependencies.getAuthorization());
    }

    @Test
    void rejectsNestedAuthWithoutClearingOuterAuthorization() {
        dependencies.setAuthBlockActive(true);
        dependencies.setAuthorization(authorization);
        assertThrows(DefaultFrameworkException.class,
                () -> interpreter.acceptImpl(command(true), new CommandResult()));
        assertSame(authorization, dependencies.getAuthorization());
        assertTrue(dependencies.isAuthBlockActive());
        verifyNoInteractions(authFactory, commandRunner);
    }

    private Auth command(final boolean autoLogout) {
        Auth auth = new Auth();
        auth.setStrategyAlias("JWT");
        auth.setCredentials("creds.json");
        auth.setAutoLogout(autoLogout);
        return auth;
    }

    private void stubAuthentication(final String alias) {
        when(authFactory.create(dependencies, alias)).thenReturn(authStrategy);
        doAnswer(call -> {
            dependencies.setAuthorization(authorization);
            return null;
        }).when(authStrategy).authenticate(any(), any());
    }
}
