package com.testlum.testing.framework.interpreter.lib;

import com.testlum.testing.framework.scenario.ScenarioContext;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link InterpreterDependencies} verifying builder construction,
 * optional bean retrieval, and authorization handling.
 */
class InterpreterDependenciesTest {

    @Test
    void builderCreatesInstance() {
        final ApplicationContext ctx = mock(ApplicationContext.class);
        final ScenarioContext scenarioCtx = new ScenarioContext(new HashMap<>());
        final InterpreterDependencies deps = InterpreterDependencies.builder()
                .context(ctx)
                .file(new File("test.xml"))
                .scenarioContext(scenarioCtx)
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        assertNotNull(deps);
        assertEquals(ctx, deps.getContext());
        assertEquals("test", deps.getEnvironment());
        assertNull(deps.getWebDriver());
    }

    @Test
    void getOptionalBeanReturnsBeanWhenExists() {
        final ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.containsBean("myBean")).thenReturn(true);
        when(ctx.getBean("myBean", String.class)).thenReturn("beanValue");

        final InterpreterDependencies deps = InterpreterDependencies.builder()
                .context(ctx)
                .file(new File("test.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        assertEquals("beanValue", deps.getOptionalBean("myBean", String.class, () -> "default"));
    }

    @Test
    void getOptionalBeanReturnsDefaultWhenMissing() {
        final ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.containsBean("myBean")).thenReturn(false);

        final InterpreterDependencies deps = InterpreterDependencies.builder()
                .context(ctx)
                .file(new File("test.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        assertEquals("default", deps.getOptionalBean("myBean", String.class, () -> "default"));
    }

    @Test
    void authorizationSetAndGet() {
        final InterpreterDependencies deps = InterpreterDependencies.builder()
                .context(mock(ApplicationContext.class))
                .file(new File("test.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        assertNull(deps.getAuthorization());

        final Map<String, String> headers = Map.of("Authorization", "Bearer token");
        final InterpreterDependencies.Authorization auth =
                new InterpreterDependencies.Authorization(headers);
        deps.setAuthorization(auth);

        assertNotNull(deps.getAuthorization());
        assertEquals("Bearer token", deps.getAuthorization().getHeaders().get("Authorization"));
    }

    @Test
    void toBuilderCopiesFieldsWithNewFile() {
        final ApplicationContext ctx = mock(ApplicationContext.class);
        final ScenarioContext scenarioCtx = new ScenarioContext(new HashMap<>());
        final AtomicInteger pos = new AtomicInteger(5);
        final InterpreterDependencies.Authorization auth =
                new InterpreterDependencies.Authorization(Map.of("Auth", "token"));
        final InterpreterDependencies original = InterpreterDependencies.builder()
                .context(ctx)
                .file(new File("main.xml"))
                .scenarioContext(scenarioCtx)
                .position(pos)
                .environment("test")
                .authorization(auth)
                .build();

        final File includedFile = new File("included.xml");
        final InterpreterDependencies copy = original.toBuilder()
                .file(includedFile)
                .build();

        assertEquals(includedFile, copy.getFile());
        assertEquals(ctx, copy.getContext());
        assertEquals(scenarioCtx, copy.getScenarioContext());
        assertEquals(pos, copy.getPosition());
        assertEquals("test", copy.getEnvironment());
        assertEquals(auth, copy.getAuthorization());
    }
}
