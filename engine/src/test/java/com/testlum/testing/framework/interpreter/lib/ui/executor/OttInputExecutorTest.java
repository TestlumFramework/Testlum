package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.OttGenerator;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.scenario.ScenarioContext;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.UiUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.OttInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OttInputExecutorTest {

    @Mock
    private OttGenerator ottGenerator;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private OttInputExecutor executor;
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
        executor = new OttInputExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "ottGenerator", ottGenerator);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {

        @Test
        void sendsGeneratedCodeToElementAndRecordsMetadata() {
            final OttInput ottInput = new OttInput();
            ottInput.setName("otpCode");
            ottInput.setAlias("myAlias");
            ottInput.setLocator("otpField");
            when(ottGenerator.generateCode("myAlias")).thenReturn("123456");
            final WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("otpField"), any(), eq(ElementChecks.FOR_WRITING)))
                    .thenReturn(element);
            final CommandResult result = new CommandResult();

            executor.execute(ottInput, result);

            verify(element).sendKeys("123456");
            verify(uiUtil).highlightElementIfRequired(anyBoolean(), eq(element), eq(driver));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
            assertEquals("otpField", result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
            assertEquals("123456", result.getMetadata().get(ResultUtil.INPUT_VALUE));
            assertEquals("myAlias", result.getMetadata().get("Alias"));
            assertEquals("123456", scenarioContext.get("otpCode"));
        }

        @Test
        void usesDefaultAliasWhenNoneProvided() {
            final OttInput ottInput = new OttInput();
            ottInput.setName("otpCode");
            ottInput.setLocator("otpField");
            when(ottGenerator.generateCode("DEFAULT")).thenReturn("654321");
            final WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("otpField"), any(), eq(ElementChecks.FOR_WRITING)))
                    .thenReturn(element);
            final CommandResult result = new CommandResult();

            executor.execute(ottInput, result);

            verify(element).sendKeys("654321");
            assertEquals("DEFAULT", result.getMetadata().get("Alias"));
            assertEquals("654321", scenarioContext.get("otpCode"));
        }

        @Test
        void doesNotStoreInScenarioContextWhenNameIsOmitted() {
            final OttInput ottInput = new OttInput();
            ottInput.setLocator("otpField");
            when(ottGenerator.generateCode("DEFAULT")).thenReturn("111222");
            final WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("otpField"), any(), eq(ElementChecks.FOR_WRITING)))
                    .thenReturn(element);
            final CommandResult result = new CommandResult();

            executor.execute(ottInput, result);

            verify(element).sendKeys("111222");
            assertFalse(scenarioContext.containsKey(null));
        }
    }
}
