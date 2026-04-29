package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.UiType;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.UiLogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.BrowserTab;
import com.knubisoft.testlum.testing.model.scenario.CloseTab;
import com.knubisoft.testlum.testing.model.scenario.OpenTab;
import com.knubisoft.testlum.testing.model.scenario.SwitchTab;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WindowType;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertThrows;
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
    private UiLogUtil uiLogUtil;
    @Mock
    private ApplicationContext context;

    private BrowserTabExecutor createExecutor(final WebDriver driver, final UiType uiType) {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .uiType(uiType)
                .environment("dev")
                .build();
        BrowserTabExecutor exec = new BrowserTabExecutor(dependencies);
        ReflectionTestUtils.setField(exec, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(exec, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(exec, "javascriptUtil", javascriptUtil);
        ReflectionTestUtils.setField(exec, "logUtil", logUtil);
        ReflectionTestUtils.setField(exec, "uiLogUtil", uiLogUtil);
        return exec;
    }

    private WebDriver createDriverWithTabs(final String... tabHandles) {
        WebDriver driver = mock(WebDriver.class);
        Set<String> handles = new LinkedHashSet<>();
        for (String h : tabHandles) {
            handles.add(h);
        }
        when(driver.getWindowHandles()).thenReturn(handles);
        WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
        lenient().when(driver.switchTo()).thenReturn(targetLocator);
        lenient().when(targetLocator.window(anyString())).thenReturn(driver);
        lenient().when(targetLocator.newWindow(any())).thenReturn(driver);
        return driver;
    }

    @Nested
    class OpenTabTest {

        @Test
        void opensNewTabInWebMode() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            OpenTab openTab = new OpenTab();
            browserTab.setOpen(openTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(driver.switchTo()).newWindow(WindowType.TAB);
            verify(resultUtil).addOpenTabMetadata(any(), eq(result));
            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }

        @Test
        void opensNewTabWithUrlInWebMode() {
            final WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            final BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);
            WebDriver.Navigation navigation = mock(WebDriver.Navigation.class);
            when(driver.navigate()).thenReturn(navigation);
            when(uiUtil.getUrl(eq("/page"), eq("dev"), eq(UiType.WEB))).thenReturn("http://localhost/page");

            BrowserTab browserTab = new BrowserTab();
            OpenTab openTab = new OpenTab();
            openTab.setUrl("/page");
            browserTab.setOpen(openTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(driver.navigate()).to("http://localhost/page");
        }

        @Test
        void opensNewTabInMobileBrowserMode() {
            final WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            // For mobile browser, need a second getWindowHandles call after window.open()
            Set<String> updatedHandles = new LinkedHashSet<>();
            updatedHandles.add("tab-1");
            updatedHandles.add("tab-2");
            updatedHandles.add("tab-3");
            when(driver.getWindowHandles())
                    .thenReturn(new LinkedHashSet<>(Set.of("tab-1", "tab-2")))
                    .thenReturn(updatedHandles);
            BrowserTabExecutor executor = createExecutor(driver, UiType.MOBILE_BROWSER);

            BrowserTab browserTab = new BrowserTab();
            OpenTab openTab = new OpenTab();
            browserTab.setOpen(openTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(javascriptUtil).executeJsScript(eq("window.open()"), eq(driver));
            verify(uiLogUtil).logOpenTabCommand(any());
        }
    }

    @Nested
    class CloseTabTest {

        @Test
        void closesLastTabWhenIndexIsNull() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            CloseTab closeTab = new CloseTab();
            browserTab.setClose(closeTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(uiLogUtil).logCloseOrSwitchTabCommand(eq(ResultUtil.CLOSE_TAB), any());
        }

        @Test
        void closesTabBySpecificIndex() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2", "tab-3");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            CloseTab closeTab = new CloseTab();
            closeTab.setIndex(2);
            browserTab.setClose(closeTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(resultUtil).addCloseOrSwitchTabMetadata(eq(ResultUtil.CLOSE_COMMAND), eq(2), eq(result));
        }

        @Test
        void throwsWhenClosingWithOnlyOneTab() {
            WebDriver driver = createDriverWithTabs("tab-1");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            CloseTab closeTab = new CloseTab();
            browserTab.setClose(closeTab);
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(browserTab, result));
        }

        @Test
        void throwsWhenTabIndexOutOfBounds() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            CloseTab closeTab = new CloseTab();
            closeTab.setIndex(5);
            browserTab.setClose(closeTab);
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(browserTab, result));
        }
    }

    @Nested
    class SwitchTabTest {

        @Test
        void switchesToLastTabWhenIndexIsNull() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            SwitchTab switchTab = new SwitchTab();
            browserTab.setSwitch(switchTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(uiLogUtil).logCloseOrSwitchTabCommand(eq(ResultUtil.SWITCH_TAB), any());
        }

        @Test
        void switchesToTabBySpecificIndex() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2", "tab-3");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            SwitchTab switchTab = new SwitchTab();
            switchTab.setIndex(1);
            browserTab.setSwitch(switchTab);
            CommandResult result = new CommandResult();

            executor.execute(browserTab, result);

            verify(driver.switchTo()).window("tab-1");
            verify(resultUtil).addCloseOrSwitchTabMetadata(eq(ResultUtil.SWITCH_COMMAND), eq(1), eq(result));
        }

        @Test
        void throwsWhenSwitchingWithOnlyOneTab() {
            WebDriver driver = createDriverWithTabs("tab-1");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            SwitchTab switchTab = new SwitchTab();
            browserTab.setSwitch(switchTab);
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(browserTab, result));
        }

        @Test
        void throwsWhenSwitchIndexOutOfBounds() {
            WebDriver driver = createDriverWithTabs("tab-1", "tab-2");
            BrowserTabExecutor executor = createExecutor(driver, UiType.WEB);

            BrowserTab browserTab = new BrowserTab();
            SwitchTab switchTab = new SwitchTab();
            switchTab.setIndex(10);
            browserTab.setSwitch(switchTab);
            CommandResult result = new CommandResult();

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(browserTab, result));
        }
    }
}
