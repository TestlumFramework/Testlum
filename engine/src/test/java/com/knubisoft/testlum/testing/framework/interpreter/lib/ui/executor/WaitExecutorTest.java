package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import com.knubisoft.testlum.testing.model.scenario.UiWait;
import com.knubisoft.testlum.testing.model.scenario.Visible;
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

import java.util.concurrent.TimeUnit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private ConfigUtil configUtil;
    @Mock
    private WaitUtil waitUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private WaitExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new WaitExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "configUtil", configUtil);
        ReflectionTestUtils.setField(executor, "waitUtil", waitUtil);
    }

    @Nested
    class SimpleWait {

        @Test
        void sleepsForGivenTimeWhenNoVisibleOrClickable() {
            UiWait uiWait = new UiWait();
            uiWait.setTime("5");
            uiWait.setUnit(Timeunit.SECONDS);
            CommandResult result = new CommandResult();
            when(waitUtil.getTimeUnit(any())).thenReturn(TimeUnit.SECONDS);

            executor.execute(uiWait, result);

            verify(waitUtil).sleep(5L, TimeUnit.SECONDS);
            verify(resultUtil).addWaitMetaData(eq("5"), eq(TimeUnit.SECONDS), eq(result));
        }
    }

    @Nested
    class WaitForVisible {

        @Test
        void waitsForElementVisibilityWhenVisibleIsSet() {
            UiWait uiWait = new UiWait();
            uiWait.setTime("10");
            uiWait.setUnit(Timeunit.SECONDS);
            Visible visible = new Visible();
            visible.setLocator("loading-spinner");
            uiWait.setVisible(visible);
            CommandResult result = new CommandResult();
            when(waitUtil.getTimeUnit(any())).thenReturn(TimeUnit.SECONDS);
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("loading-spinner"), any())).thenReturn(element);

            executor.execute(uiWait, result);

            verify(uiUtil).findWebElement(any(), eq("loading-spinner"), any());
        }
    }

    @Nested
    class ExceptionHandling {

        @Test
        void handlesExceptionGracefully() {
            UiWait uiWait = new UiWait();
            uiWait.setTime("invalid");
            CommandResult result = new CommandResult();
            when(waitUtil.getTimeUnit(any())).thenReturn(TimeUnit.SECONDS);

            executor.execute(uiWait, result);

            verify(logUtil).logException(any(Exception.class));
            verify(resultUtil).setExceptionResult(eq(result), any(Exception.class));
        }
    }
}
