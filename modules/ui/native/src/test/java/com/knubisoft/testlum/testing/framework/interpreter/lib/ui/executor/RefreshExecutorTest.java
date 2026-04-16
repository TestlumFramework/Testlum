package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Refresh;
import io.appium.java_client.AppiumDriver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Sequence;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RefreshExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private AppiumDriver appiumDriver;
    @Mock
    private ApplicationContext context;

    private RefreshExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        WebDriver.Options options = mock(WebDriver.Options.class);
        WebDriver.Window window = mock(WebDriver.Window.class);
        when(appiumDriver.manage()).thenReturn(options);
        when(options.window()).thenReturn(window);
        when(window.getSize()).thenReturn(new Dimension(1080, 1920));
        Sequence mockSequence = mock(Sequence.class);
        when(uiUtil.buildSequence(any(), any(), anyInt())).thenReturn(mockSequence);

        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(appiumDriver)
                .build();
        executor = new RefreshExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {

        @Test
        void performsRefreshSwipeAndTakesScreenshot() {
            Refresh refresh = new Refresh();
            CommandResult result = new CommandResult();

            executor.execute(refresh, result);

            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }
}
