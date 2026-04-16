package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Input;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InputExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private InputExecutor executor;
    private File scenarioFile;

    @BeforeEach
    void setUp() {
        scenarioFile = mock(File.class);
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .file(scenarioFile)
                .build();
        executor = new InputExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {

        @Test
        void sendsKeysToElementAndRecordsMetadata() {
            Input input = new Input();
            input.setLocator("username");
            input.setValue("testUser");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("username"), any())).thenReturn(element);
            when(uiUtil.resolveSendKeysType(eq("testUser"), eq(element), eq(scenarioFile)))
                    .thenReturn("testUser");

            executor.execute(input, result);

            verify(element).sendKeys("testUser");
            verify(uiUtil).waitForElementVisibility(any(), eq(element));
            verify(uiUtil).highlightElementIfRequired(anyBoolean(), eq(element), eq(driver));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
            assertEquals("username", result.getMetadata().get(ResultUtil.INPUT_LOCATOR));
            assertEquals("testUser", result.getMetadata().get(ResultUtil.INPUT_VALUE));
        }
    }
}
