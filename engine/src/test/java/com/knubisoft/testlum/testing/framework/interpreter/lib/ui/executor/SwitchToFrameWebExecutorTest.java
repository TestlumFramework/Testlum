package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.UiLogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.util.check.AbstractElementCheck;
import com.knubisoft.testlum.testing.model.scenario.SwitchToFrame;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SwitchToFrameWebExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiLogUtil uiLogUtil;
    @Mock
    private SubCommandRunnerImpl subCommandRunner;
    @Mock
    private WebDriver driver;
    @Mock
    private WebDriver.TargetLocator targetLocator;
    @Mock
    private ApplicationContext context;

    private SwitchToFrameWebExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(driver.switchTo()).thenReturn(targetLocator);
        lenient().when(targetLocator.frame(any(WebElement.class))).thenReturn(driver);
        lenient().when(targetLocator.frame(any(int.class))).thenReturn(driver);
        when(targetLocator.parentFrame()).thenReturn(driver);
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new SwitchToFrameWebExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiLogUtil", uiLogUtil);
        ReflectionTestUtils.setField(executor, "subCommandRunner", subCommandRunner);
    }

    @Nested
    class SwitchByLocator {

        @Test
        void switchesToFrameByLocatorAndRunsSubCommands() {
            SwitchToFrame frame = new SwitchToFrame();
            frame.setLocator("iframe-main");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("iframe-main"), any(), any(AbstractElementCheck[].class)))
                    .thenReturn(element);

            executor.execute(frame, result);

            verify(targetLocator).frame(element);
            verify(subCommandRunner).runCommands(any(), eq(result), any());
            verify(uiLogUtil).startUiCommandsInFrame();
            verify(uiLogUtil).endUiCommandsInFrame();
            verify(targetLocator).parentFrame();
            assertEquals("iframe-main", result.getMetadata().get(ResultUtil.SWITCH_LOCATOR));
        }
    }

    @Nested
    class SwitchByIndex {

        @Test
        void switchesToFrameByIndexAndRunsSubCommands() {
            SwitchToFrame frame = new SwitchToFrame();
            frame.setIndex("2");
            CommandResult result = new CommandResult();

            executor.execute(frame, result);

            verify(targetLocator).frame(2);
            verify(subCommandRunner).runCommands(any(), eq(result), any());
            verify(targetLocator).parentFrame();
            assertEquals("2", result.getMetadata().get(ResultUtil.SWITCH_INDEX));
        }
    }
}
