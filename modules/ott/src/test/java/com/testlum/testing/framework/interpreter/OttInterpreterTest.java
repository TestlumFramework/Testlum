package com.testlum.testing.framework.interpreter;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.configuration.ConfigProvider;
import com.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ConditionProvider;
import com.testlum.testing.framework.util.JacksonService;
import com.testlum.testing.framework.util.StringPrettifier;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.context.ApplicationContext;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class OttInterpreterTest {

    @TempDir
    File tempDir;

    private OttUtil ottUtil;
    private OttInterpreter ottInterpreter;

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
        ottUtil = mock(OttUtil.class);
        when(applicationContext.getBean(OttUtil.class)).thenReturn(ottUtil);

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(new ScenarioContext(new HashMap<>()))
                .position(new AtomicInteger())
                .environment("test")
                .build();

        ottInterpreter = new OttInterpreter(dependencies);
    }

    @Nested
    class Construction {

        @Test
        void interpreterIsNotNull() {
            assertNotNull(ottInterpreter);
        }

        @Test
        void resolvesOttUtilFromContext() throws Exception {
            final Field field = OttInterpreter.class.getDeclaredField("ottUtil");
            field.setAccessible(true);

            assertEquals(ottUtil, field.get(ottInterpreter));
        }
    }

    @Nested
    class AddOttMetaData {

        @Test
        void addsAllMetaDataToResult() throws Exception {
            final CommandResult result = new CommandResult();

            final Method method = OttInterpreter.class.getDeclaredMethod(
                    "addOttMetaData", String.class, String.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(ottInterpreter, "myAlias", "123456", result);

            assertEquals("myAlias", result.getMetadata().get("Alias"));
        }
    }
}
