package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.util.HttpUtil;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.framework.util.JacksonService;
import com.knubisoft.testlum.testing.framework.util.StringPrettifier;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class HttpInterpreterTest {

    @TempDir
    File tempDir;

    private HttpInterpreter interpreter;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(applicationContext.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        when(applicationContext.getBean(IntegrationsProvider.class)).thenReturn(mock(IntegrationsProvider.class));
        when(applicationContext.getBean(HttpUtil.class)).thenReturn(mock(HttpUtil.class));
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger(0))
                .environment("test")
                .build();

        interpreter = new HttpInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class AddHttpMetaData {

        @Test
        void addsMetadataWithoutHeaders() throws Exception {
            final CommandResult result = new CommandResult();
            final Map<String, String> headers = new LinkedHashMap<>();

            final Method method = HttpInterpreter.class.getDeclaredMethod(
                    "addHttpMetaData", String.class, String.class, Map.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, "testAlias", "GET", headers, "/api/test", result);

            assertEquals("testAlias", result.getMetadata().get("API alias"));
            assertEquals("/api/test", result.getMetadata().get("Endpoint"));
            assertEquals("GET", result.getMetadata().get("HTTP method"));
        }

        @Test
        void addsMetadataWithHeaders() throws Exception {
            final CommandResult result = new CommandResult();
            final Map<String, String> headers = new LinkedHashMap<>();
            headers.put("Authorization", "Bearer token");

            final Method method = HttpInterpreter.class.getDeclaredMethod(
                    "addHttpMetaData", String.class, String.class, Map.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, "alias", "POST", headers, "/api/data", result);

            assertEquals("alias", result.getMetadata().get("API alias"));
            assertEquals("POST", result.getMetadata().get("HTTP method"));
            assertTrue(result.getMetadata().containsKey("Additional headers"));
        }
    }
}
