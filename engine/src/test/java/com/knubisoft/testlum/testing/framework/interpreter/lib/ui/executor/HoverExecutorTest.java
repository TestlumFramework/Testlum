package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioContext;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.UiLogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Interactive;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HoverExecutorTest {

    interface InteractiveWebDriver extends WebDriver, Interactive { }

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private ConditionUtil conditionUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiLogUtil uiLogUtil;
    @Mock
    private InteractiveWebDriver driver;
    @Mock
    private ScenarioContext scenarioContext;
    @Mock
    private ApplicationContext context;

    private HoverExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .scenarioContext(scenarioContext)
                .build();
        executor = new HoverExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "conditionUtil", conditionUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiLogUtil", uiLogUtil);
    }

    @Nested
    class Execute {

        @Test
        void addsMetadataAndLogsHover() {
            Hover hover = new Hover();
            hover.setLocator("menu-item");
            CommandResult result = new CommandResult();
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(false);

            executor.execute(hover, result);

            verify(resultUtil).addHoverMetaData(eq(hover), eq(result));
            verify(uiLogUtil).logHover(eq(hover));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }

        @Test
        void findsAndMovesToElementWhenConditionIsTrue() {
            Hover hover = new Hover();
            hover.setLocator("link-hover");
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(true);
            when(uiUtil.findWebElement(any(), eq("link-hover"), any())).thenReturn(element);

            executor.execute(hover, result);

            verify(uiUtil).findWebElement(any(), eq("link-hover"), any());
        }

        @Test
        void skipsHoverWhenConditionIsFalse() {
            Hover hover = new Hover();
            hover.setLocator("hidden-link");
            CommandResult result = new CommandResult();
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(false);

            executor.execute(hover, result);

            verify(uiUtil, never()).findWebElement(any(), any(), any());
        }
    }

    @Nested
    class MoveToEmptySpace {

        @Test
        void movesToEmptySpaceWhenFlagIsTrue() {
            Hover hover = new Hover();
            hover.setLocator("tooltip-trigger");
            hover.setMoveToEmptySpace(true);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(true);
            when(uiUtil.findWebElement(any(), eq("tooltip-trigger"), any())).thenReturn(element);

            WebElement htmlElement = mock(WebElement.class);
            when(driver.findElement(any())).thenReturn(htmlElement);

            executor.execute(hover, result);

            verify(driver).findElement(any());
        }

        @Test
        void doesNotMoveToEmptySpaceWhenFlagIsFalse() {
            Hover hover = new Hover();
            hover.setLocator("simple-hover");
            hover.setMoveToEmptySpace(false);
            CommandResult result = new CommandResult();
            WebElement element = mock(WebElement.class);
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(true);
            when(uiUtil.findWebElement(any(), eq("simple-hover"), any())).thenReturn(element);

            executor.execute(hover, result);

            verify(driver, never()).findElement(any());
        }

        @Test
        void movesToEmptySpaceEvenWhenConditionIsFalse() {
            Hover hover = new Hover();
            hover.setLocator("hover-target");
            hover.setMoveToEmptySpace(true);
            CommandResult result = new CommandResult();
            when(conditionUtil.isTrue(any(), eq(scenarioContext), eq(result))).thenReturn(false);

            WebElement htmlElement = mock(WebElement.class);
            when(driver.findElement(any())).thenReturn(htmlElement);

            executor.execute(hover, result);

            verify(driver).findElement(any());
            verify(uiUtil, never()).findWebElement(any(), any(), any());
        }
    }
}
