package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.OttUtil;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.model.scenario.UiOtt;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OttExecutorTest {

    @Mock
    private OttUtil ottUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private OttExecutor executor;
    private ScenarioContext scenarioContext;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        scenarioContext = new ScenarioContext(new HashMap<>());
        final ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(mock(File.class))
                .scenarioContext(scenarioContext)
                .build();
        executor = new OttExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "ottUtil", ottUtil);
    }

    @Nested
    class Execute {

        @Test
        void generatesCodeAndStoresItInScenarioContext() {
            final UiOtt uiOtt = new UiOtt();
            uiOtt.setName("otpCode");
            uiOtt.setAlias("myAlias");
            when(ottUtil.generateCode("myAlias")).thenReturn("123456");
            final CommandResult result = new CommandResult();

            executor.execute(uiOtt, result);

            assertEquals("123456", scenarioContext.get("otpCode"));
        }

        @Test
        void usesDefaultAliasWhenNoneProvided() {
            final UiOtt uiOtt = new UiOtt();
            uiOtt.setName("otpCode");
            when(ottUtil.generateCode("DEFAULT")).thenReturn("654321");
            final CommandResult result = new CommandResult();

            executor.execute(uiOtt, result);

            assertEquals("DEFAULT", result.getMetadata().get("Alias"));
            assertEquals("654321", scenarioContext.get("otpCode"));
        }

        @Test
        void addsMetaDataWithProvidedAlias() {
            final UiOtt uiOtt = new UiOtt();
            uiOtt.setName("otpCode");
            uiOtt.setAlias("someAlias");
            when(ottUtil.generateCode("someAlias")).thenReturn("999999");
            final CommandResult result = new CommandResult();

            executor.execute(uiOtt, result);
            scenarioContext.get("otpCode");

            assertEquals("someAlias", result.getMetadata().get("Alias"));
        }

        @Test
        void refreshFalseKeepsCachedCodeAcrossReads() {
            final UiOtt uiOtt = new UiOtt();
            uiOtt.setName("otpCode");
            uiOtt.setAlias("myAlias");
            uiOtt.setRefresh(false);
            when(ottUtil.generateCode("myAlias")).thenReturn("111111", "222222");
            final CommandResult result = new CommandResult();

            executor.execute(uiOtt, result);

            assertEquals("111111", scenarioContext.get("otpCode"));
            assertEquals("111111", scenarioContext.get("otpCode"));
        }

        @Test
        void refreshTrueRecomputesCodeOnEveryRead() {
            final UiOtt uiOtt = new UiOtt();
            uiOtt.setName("otpCode");
            uiOtt.setAlias("myAlias");
            uiOtt.setRefresh(true);
            when(ottUtil.generateCode("myAlias")).thenReturn("111111", "222222");
            final CommandResult result = new CommandResult();

            executor.execute(uiOtt, result);

            assertEquals("111111", scenarioContext.get("otpCode"));
            assertEquals("222222", scenarioContext.get("otpCode"));
        }
    }
}
