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
import com.testlum.testing.model.scenario.Ott;
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
import static org.mockito.Mockito.*;

public class OttInterpreterTest {

    @TempDir
    File tempDir;

    private OttGenerator ottGenerator;
    private OttInterpreter ottInterpreter;
    private ScenarioContext scenarioContext;
    private JacksonService jacksonService;

    @BeforeEach
    void setUp() {
        final ApplicationContext applicationContext = mock(ApplicationContext.class);
        when(applicationContext.getBean(ConfigProvider.class)).thenReturn(mock(ConfigProvider.class));
        when(applicationContext.getBean(ConditionProvider.class)).thenReturn(mock(ConditionProvider.class));
        when(applicationContext.getBean(FileSearcher.class)).thenReturn(mock(FileSearcher.class));
        jacksonService = mock(JacksonService.class);
        when(applicationContext.getBean(JacksonService.class)).thenReturn(jacksonService);
        when(applicationContext.getBean(StringPrettifier.class)).thenReturn(mock(StringPrettifier.class));
        final GlobalTestConfiguration globalConfig = mock(GlobalTestConfiguration.class);
        when(globalConfig.isStopScenarioOnFailure()).thenReturn(false);
        when(applicationContext.getBean(GlobalTestConfiguration.class)).thenReturn(globalConfig);
        ottGenerator = mock(OttGenerator.class);
        when(applicationContext.getBean(OttGenerator.class)).thenReturn(ottGenerator);

        scenarioContext = new ScenarioContext(new HashMap<>());

        final InterpreterDependencies dependencies = InterpreterDependencies.builder()
                .context(applicationContext)
                .file(new File(tempDir, "scenario.xml"))
                .scenarioContext(scenarioContext)
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
        void resolvesOttGeneratorFromContext() throws Exception {
            final Field field = OttInterpreter.class.getDeclaredField("ottGenerator");
            field.setAccessible(true);

            assertEquals(ottGenerator, field.get(ottInterpreter));
        }
    }

    @Nested
    class AcceptImpl {

        @Test
        void generatesCodeAndStoresItInScenarioContext() throws Exception {
            final Ott ott = new Ott();
            ott.setName("otpCode");
            ott.setAlias("myAlias");
            when(jacksonService.writeValueToCopiedString(ott)).thenReturn("json");
            when(jacksonService.readCopiedValue("json", Ott.class)).thenReturn(ott);
            when(ottGenerator.generateCode("myAlias")).thenReturn("123456");
            final CommandResult result = new CommandResult();

            final Method method = OttInterpreter.class.getDeclaredMethod(
                    "acceptImpl", Ott.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(ottInterpreter, ott, result);

            assertEquals("123456", scenarioContext.get("otpCode"));
            assertEquals("myAlias", result.getMetadata().get("Alias"));
        }

        @Test
        void usesDefaultAliasWhenNoneProvided() throws Exception {
            final Ott ott = new Ott();
            ott.setName("otpCode");
            when(jacksonService.writeValueToCopiedString(ott)).thenReturn("json");
            when(jacksonService.readCopiedValue("json", Ott.class)).thenReturn(ott);
            when(ottGenerator.generateCode("DEFAULT")).thenReturn("654321");
            final CommandResult result = new CommandResult();

            final Method method = OttInterpreter.class.getDeclaredMethod(
                    "acceptImpl", Ott.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(ottInterpreter, ott, result);

            assertEquals("DEFAULT", result.getMetadata().get("Alias"));
            assertEquals("654321", scenarioContext.get("otpCode"));
        }

        @Test
        void refreshFalseKeepsCachedCodeAcrossReads() throws Exception {
            final Ott ott = new Ott();
            ott.setName("otpCode");
            ott.setAlias("myAlias");
            ott.setRefresh(false);
            when(jacksonService.writeValueToCopiedString(ott)).thenReturn("json");
            when(jacksonService.readCopiedValue("json", Ott.class)).thenReturn(ott);
            when(ottGenerator.generateCode("myAlias")).thenReturn("111111", "222222");
            final CommandResult result = new CommandResult();

            final Method method = OttInterpreter.class.getDeclaredMethod(
                    "acceptImpl", Ott.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(ottInterpreter, ott, result);

            assertEquals("111111", scenarioContext.get("otpCode"));
            assertEquals("111111", scenarioContext.get("otpCode"));
        }

        @Test
        void refreshTrueRecomputesCodeOnEveryRead() throws Exception {
            final Ott ott = new Ott();
            ott.setName("otpCode");
            ott.setAlias("myAlias");
            ott.setRefresh(true);
            when(jacksonService.writeValueToCopiedString(ott)).thenReturn("json");
            when(jacksonService.readCopiedValue("json", Ott.class)).thenReturn(ott);
            when(ottGenerator.generateCode("myAlias")).thenReturn("111111", "222222");
            final CommandResult result = new CommandResult();

            final Method method = OttInterpreter.class.getDeclaredMethod(
                    "acceptImpl", Ott.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(ottInterpreter, ott, result);

            assertEquals("111111", scenarioContext.get("otpCode"));
            assertEquals("222222", scenarioContext.get("otpCode"));
        }

        @Test
        void usesSecretDirectlyWhenProvided() throws Exception {
            final Ott ott = new Ott();
            ott.setName("otpCode");
            ott.setSecret("JBSWY3DPEHPK3PXP");
            when(jacksonService.writeValueToCopiedString(ott)).thenReturn("json");
            when(jacksonService.readCopiedValue("json", Ott.class)).thenReturn(ott);
            when(ottGenerator.generateCodeFromSecret("JBSWY3DPEHPK3PXP")).thenReturn("111111");
            final CommandResult result = new CommandResult();

            final Method method = OttInterpreter.class.getDeclaredMethod(
                    "acceptImpl", Ott.class, CommandResult.class);
            method.setAccessible(true);
            method.invoke(ottInterpreter, ott, result);

            assertEquals("111111", scenarioContext.get("otpCode"));
            verify(ottGenerator, never()).generateCode(any());
        }
    }
}
