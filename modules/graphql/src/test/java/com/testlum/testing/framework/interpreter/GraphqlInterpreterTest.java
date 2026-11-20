package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.IntegrationsProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.scenario.Graphql;
import com.testlum.testing.model.scenario.HttpInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GraphqlInterpreterTest {

    private GraphqlInterpreter interpreter;

    @BeforeEach
    void setUp() {
        final ApplicationContext ctx = mock(ApplicationContext.class);
        when(ctx.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(ctx.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(ctx.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(ctx.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(ctx.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(ctx.getBean(GlobalTestConfiguration.class)).thenReturn(mock(GlobalTestConfiguration.class));
        when(ctx.getBean(IntegrationsProvider.class)).thenReturn(mock(IntegrationsProvider.class));
        when(ctx.getBean(HttpUtil.class)).thenReturn(mock(HttpUtil.class));

        final InterpreterDependencies deps = InterpreterDependencies.builder()
                .context(ctx)
                .file(new File("scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new GraphqlInterpreter(deps);
    }

    @Nested
    class Annotation {
        @Test
        void hasInterpreterForClassAnnotation() {
            final InterpreterForClass annotation = GraphqlInterpreter.class
                    .getAnnotation(InterpreterForClass.class);
            assertNotNull(annotation);
            assertEquals(Graphql.class, annotation.value());
        }
    }

    @Nested
    class PrettifyString {
        @Test
        void removeExtraWhitespace() {
            assertEquals("a b c", interpreter.prettifyString("a   b   c"));
        }

        @Test
        void singleSpacesUnchanged() {
            assertEquals("hello world", interpreter.prettifyString("hello world"));
        }

        @Test
        void tabsAndNewlinesReplaced() {
            final String result = interpreter.prettifyString("a\t\tb");
            assertEquals("a b", result);
        }
    }

    @Nested
    class AddGraphQlMetaData {
        @Test
        void populatesMetadataWithoutHeaders() {
            final CommandResult result = new CommandResult();
            interpreter.prettifyString("test");

            result.put("Alias", "myAlias");
            result.put("HTTP method", HttpMethod.POST);
            result.put("Endpoint", "/graphql");

            assertEquals("myAlias", result.getMetadata().get("Alias"));
            assertEquals(HttpMethod.POST, result.getMetadata().get("HTTP method"));
            assertEquals("/graphql", result.getMetadata().get("Endpoint"));
        }

        @Test
        void populatesMetadataWithMaskedAuthorization() throws Exception {
            final CommandResult result = new CommandResult();
            final Map<String, String> headers = Map.of("authorization", "Bearer token");
            final Method method = GraphqlInterpreter.class.getDeclaredMethod(
                    "addGraphQlMetaData", String.class, HttpMethod.class, Map.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, "alias1", HttpMethod.GET, headers, "/query", result);

            assertEquals("alias1", result.getMetadata().get("Alias"));
            assertEquals("/query", result.getMetadata().get("Endpoint"));
            assertTrue(result.getMetadata().get("Additional headers").toString()
                    .contains("authorization: Bearer tok***"));
            assertFalse(result.getMetadata().toString().contains("Bearer token"));
            assertEquals("Bearer token", headers.get("authorization"));
        }
    }

    @Nested
    class GraphqlMetadataInnerClass {
        @Test
        void storesHttpInfoAndMethod() {
            final HttpInfo httpInfo = mock(HttpInfo.class);
            final GraphqlInterpreter.GraphqlMetadata metadata =
                    new GraphqlInterpreter.GraphqlMetadata(httpInfo, HttpMethod.POST);
            assertEquals(httpInfo, metadata.getHttpInfo());
            assertEquals(HttpMethod.POST, metadata.getHttpMethod());
        }

        @Test
        void handlesGetMethod() {
            final HttpInfo httpInfo = mock(HttpInfo.class);
            final GraphqlInterpreter.GraphqlMetadata metadata =
                    new GraphqlInterpreter.GraphqlMetadata(httpInfo, HttpMethod.GET);
            assertEquals(HttpMethod.GET, metadata.getHttpMethod());
        }

        @Test
        void handlesNullHttpInfo() {
            final GraphqlInterpreter.GraphqlMetadata metadata =
                    new GraphqlInterpreter.GraphqlMetadata(null, HttpMethod.GET);
            assertNull(metadata.getHttpInfo());
        }
    }

    @Nested
    class Constructor {
        @Test
        void createsInterpreterSuccessfully() {
            assertNotNull(interpreter);
        }
    }
}
