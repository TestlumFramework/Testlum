package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.FileSearcher;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionProvider;
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
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LambdaInterpreterTest {

    @TempDir
    File tempDir;

    private LambdaInterpreter interpreter;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        when(applicationContext.getBean(JacksonService.class)).thenReturn(mock(JacksonService.class));
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
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

        interpreter = new LambdaInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(interpreter);
        }
    }

    @Nested
    class AddLambdaGeneralMetaData {

        @Test
        void addsAllMetadataToResult() throws Exception {
            final StringPrettifier prettifier = mock(StringPrettifier.class);
            when(prettifier.asJsonResult("{\"key\":\"value\"}")).thenReturn("{\"key\":\"value\"}");

            final java.lang.reflect.Field field = LambdaInterpreter.class.getSuperclass()
                    .getDeclaredField("stringPrettifier");
            field.setAccessible(true);
            field.set(interpreter, prettifier);

            final CommandResult result = new CommandResult();
            final Method method = LambdaInterpreter.class.getDeclaredMethod(
                    "addLambdaGeneralMetaData", String.class, String.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(interpreter, "myAlias", "myFunction", "{\"key\":\"value\"}", result);

            assertEquals("myAlias", result.getMetadata().get("Alias"));
            assertEquals("myFunction", result.getMetadata().get("Function name"));
        }
    }
}
