package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.framework.auth.AuthFactory;
import com.testlum.testing.framework.auth.AuthenticationStrategy;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.interpreter.lib.*;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.scenario.ScenarioValidator;
import com.testlum.testing.framework.util.*;
import com.testlum.testing.framework.xml.XMLParsers;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AuthIncludeTest {
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    @SuppressWarnings("unchecked")
    void includedAuthRestoresOuterContext(final boolean innerAutoLogout) {
        final ApplicationContext beans = mock(ApplicationContext.class, RETURNS_DEEP_STUBS);
        final ScenarioContext context = new ScenarioContext(Map.of());
        final InterpreterDependencies dependencies = InterpreterDependencies.builder().context(beans)
                .scenarioContext(context).position(new AtomicInteger()).file(new File("outer.xml")).build();
        final AuthFactory factory = mock(AuthFactory.class);
        final CommandRunner commands = mock(CommandRunner.class);
        final ConditionProvider conditions = mock(ConditionProvider.class);
        when(beans.getBean(AuthFactory.class)).thenReturn(factory);
        when(beans.getBean(CommandRunner.class)).thenReturn(commands);
        when(beans.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(beans.getBean(ConditionProvider.class)).thenReturn(conditions);
        when(conditions.isTrue(any(), any(), any())).thenReturn(true);
        when(beans.getBean(JacksonService.class)).thenReturn(new JacksonService());
        when(beans.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(beans.getBean(GlobalTestConfiguration.class)).thenReturn(new GlobalTestConfiguration());
        final FileSearcher searcher = mock(FileSearcher.class);
        when(beans.getBean(FileSearcher.class)).thenReturn(searcher);
        final AuthInterpreter authInterpreter = new AuthInterpreter(dependencies);
        final Auth outer = auth("OUTER", true);
        final Auth inner = auth("INNER", innerAutoLogout);
        final Include include = new Include();
        include.setScenario("included");
        outer.getCommands().add(include);
        final Scenario included = new Scenario();
        included.getCommands().add(inner);
        final XMLParsers parsers = mock(XMLParsers.class, RETURNS_DEEP_STUBS);
        final ScenarioValidator validator = mock(ScenarioValidator.class);
        final File includedFile = new File("included/scenario.xml");
        when(searcher.searchFileFromDir(any(), anyString())).thenReturn(includedFile);
        when(parsers.forScenario().process(includedFile, validator)).thenReturn(included);
        final InterpreterProvider provider = mock(InterpreterProvider.class);
        when(provider.getAppropriateInterpreter(inner, dependencies))
                .thenReturn((AbstractInterpreter<AbstractCommand>) (AbstractInterpreter<?>) authInterpreter);
        final IncludedScenarioRunner runner = new IncludedScenarioRunnerImpl(parsers, searcher,
                mock(TestResourceSettings.class), new ResultUtil(), mock(ConfigUtil.class), mock(LogUtil.class),
                provider, validator);
        when(beans.getBean(IncludedScenarioRunner.class)).thenReturn(runner);
        final IncludeInterpreter includeInterpreter = new IncludeInterpreter(dependencies);
        final AuthenticationStrategy outerStrategy = strategy(factory, dependencies, "OUTER", "outer-secret");
        final AuthenticationStrategy innerStrategy = strategy(factory, dependencies, "INNER", "inner-secret");
        doAnswer(call -> {
            if (call.getArgument(0) == outer.getCommands()) {
                assertEquals("outer-secret", context.get("token"));
                final CommandResult includedResult = new CommandResult();
                includedResult.setSuccess(true);
                includeInterpreter.acceptImpl(include, includedResult);
                assertTrue(includedResult.isSuccess());
                assertEquals(1, includedResult.getSubCommandsResult().size());
                assertAuthorization(dependencies, "outer-secret");
            } else {
                assertAuthorization(dependencies, "inner-secret");
                context.setVariable("copy", context.get("token"));
            }
            return null;
        }).when(commands).runCommands(anyList(), any(), any(), anyList());
        authInterpreter.acceptImpl(outer, new CommandResult());
        assertNull(dependencies.getAuthorization());
        assertFalse(context.containsKey("token"));
        assertEquals("inner-secret", context.get("copy"));
        assertEquals("inn***", context.redact(context.get("copy")));
        assertFalse(dependencies.isAuthBlockActive());
        verify(outerStrategy).logout();
        verify(innerStrategy, times(innerAutoLogout ? 1 : 0)).logout();
    }

    private void assertAuthorization(final InterpreterDependencies dependencies, final String token) {
        assertEquals(token, dependencies.getScenarioContext().get("token"));
        assertEquals("Bearer " + token, dependencies.getAuthorization().getHeaders().get("Authorization"));
    }

    private Auth auth(final String alias, final boolean autoLogout) {
        final Auth auth = new Auth();
        auth.setStrategyAlias(alias);
        auth.setCredentials("user.json");
        auth.setTokenVariable("token");
        auth.setAutoLogout(autoLogout);
        return auth;
    }

    private AuthenticationStrategy strategy(final AuthFactory factory, final InterpreterDependencies dependencies,
                                            final String alias, final String token) {
        final AuthenticationStrategy strategy = mock(AuthenticationStrategy.class);
        when(factory.create(dependencies, alias)).thenReturn(strategy);
        when(strategy.getAccessToken()).thenReturn(Optional.of(token));
        doAnswer(call -> {
            dependencies.setAuthorization(new InterpreterDependencies.Authorization(
                    Map.of("Authorization", "Bearer " + token)));
            return null;
        }).when(strategy).authenticate(any(), any());
        return strategy;
    }
}
