package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.auth.AuthFactory;
import com.testlum.testing.framework.auth.AuthStrategy;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Logout;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class LogoutInterpreterTest {

    private LogoutInterpreter interpreter;
    private InterpreterDependencies dependencies;
    private AuthFactory authFactory;
    private AuthStrategy authStrategy;
    private JacksonService jacksonService;
    private ConditionProvider conditionProvider;

    @BeforeEach
    void setUp() {
        authFactory = mock(AuthFactory.class);
        authStrategy = mock(AuthStrategy.class);
        jacksonService = mock(JacksonService.class);
        conditionProvider = mock(ConditionProvider.class);
        GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);

        final ApplicationContext context = mock(ApplicationContext.class);
        when(context.getBean(AuthFactory.class)).thenReturn(authFactory);
        when(context.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(context.getBean(ConditionProvider.class)).thenReturn(conditionProvider);
        when(context.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(context.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(context.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(context.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        ScenarioContext scenarioContext = new ScenarioContext(new HashMap<>());
        dependencies = InterpreterDependencies.builder()
                .context(context)
                .file(new File("test.xml"))
                .scenarioContext(scenarioContext)
                .position(new AtomicInteger(1))
                .environment("test")
                .build();

        interpreter = new LogoutInterpreter(dependencies);
    }

    @Test
    void applyDelegatesToAuthStrategyLogout() {
        Logout logout = new Logout();
        logout.setAlias("api1");

        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"alias\":\"api1\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Logout.class))).thenReturn(logout);
        when(authFactory.create(any(), eq("api1"))).thenReturn(authStrategy);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        CommandResult result = new CommandResult();
        result.setId(1);
        dependencies.setAuthorization(new InterpreterDependencies.Authorization(
                Map.of("Authorization", "Bearer token")));
        interpreter.apply(logout, result);
        assertNull(dependencies.getAuthorization());

        verify(authStrategy).logout();
    }

    @Test
    void applyInjectsCommandBeforeProcessing() {
        Logout logout = new Logout();
        logout.setAlias("api2");

        Logout injectedLogout = new Logout();
        injectedLogout.setAlias("api2-injected");

        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{\"alias\":\"api2\"}");
        when(jacksonService.readCopiedValue(anyString(), eq(Logout.class))).thenReturn(injectedLogout);
        when(authFactory.create(any(), eq("api2-injected"))).thenReturn(authStrategy);
        when(conditionProvider.isTrue(any(), any(), any())).thenReturn(true);

        CommandResult result = new CommandResult();
        result.setId(1);
        dependencies.setAuthorization(new InterpreterDependencies.Authorization(
                Map.of("Authorization", "Bearer token")));
        interpreter.apply(logout, result);
        assertNull(dependencies.getAuthorization());

        verify(authFactory).create(any(), eq("api2-injected"));
    }

    @Test
    void clearsAuthorizationWhenLogoutFails() {
        Logout logout = preparedLogout("JWT");
        when(authFactory.create(dependencies, "JWT")).thenReturn(authStrategy);
        doThrow(new IllegalStateException("Logout failed")).when(authStrategy).logout();
        assertThrows(IllegalStateException.class, () -> interpreter.acceptImpl(logout, new CommandResult()));
        assertNull(dependencies.getAuthorization());
    }

    @Test
    void clearsAuthorizationWhenFactoryFails() {
        Logout logout = preparedLogout("JWT");
        when(authFactory.create(dependencies, "JWT")).thenThrow(new IllegalStateException("Strategy unavailable"));
        assertThrows(IllegalStateException.class, () -> interpreter.acceptImpl(logout, new CommandResult()));
        assertNull(dependencies.getAuthorization());
    }

    private Logout preparedLogout(final String alias) {
        Logout logout = new Logout();
        logout.setAlias(alias);
        when(jacksonService.writeValueToCopiedString(any())).thenReturn("{}");
        when(jacksonService.readCopiedValue(anyString(), eq(Logout.class))).thenReturn(logout);
        dependencies.setAuthorization(new InterpreterDependencies.Authorization(
                Map.of("Authorization", "Bearer token")));
        return logout;
    }
}
