package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.BrowserTab;
import com.knubisoft.testlum.testing.model.scenario.CloseTab;
import com.knubisoft.testlum.testing.model.scenario.OpenTab;
import com.knubisoft.testlum.testing.model.scenario.SwitchTab;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BrowserTabExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private JavascriptUtil javascriptUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private WebDriver.TargetLocator targetLocator;
    @Mock
    private ApplicationContext context;

    private BrowserTabExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        Set<String> handles = new LinkedHashSet<>();
        handles.add("tab-1");
        handles.add("tab-2");
        when(driver.getWindowHandles()).thenReturn(handles);
        when(driver.switchTo()).thenReturn(targetLocator);
        lenient().when(targetLocator.window(anyString())).thenReturn(driver);
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .uiType(UiType.WEB)
                .environment("dev")
                .build();
        executor = new BrowserTabExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "javascriptUtil", javascriptUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
    }

    @Nested
    class OpenTabTest {

        @Test
        void opensNewTabInWebMode() {
            BrowserTab browserTab = new BrowserTab();
            OpenTab openTab = new OpenTab();
            browserTab.setOpen(openTab);
            CommandResult result = new CommandResult();
            when(targetLocator.newWindow(any())).thenReturn(driver);

            executor.execute(browserTab, result);

            verify(resultUtil).addOpenTabMetadata(any(), eq(result));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }

    @Nested
    class CloseTabTest {

        @Test
        void closesLastTabWhenIndexIsNull() {
            BrowserTab browserTab = new BrowserTab();
            CloseTab closeTab = new CloseTab();
            browserTab.setClose(closeTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(targetLocator).window("tab-2");
            verify(logUtil).logCloseOrSwitchTabCommand(eq(ResultUtil.CLOSE_TAB), any());
        }
    }

    @Nested
    class SwitchTabTest {

        @Test
        void switchesToLastTabWhenIndexIsNull() {
            BrowserTab browserTab = new BrowserTab();
            SwitchTab switchTab = new SwitchTab();
            browserTab.setSwitch(switchTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(logUtil).logCloseOrSwitchTabCommand(eq(ResultUtil.SWITCH_TAB), any());
        }
    }
}
