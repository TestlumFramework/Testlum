package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.NavigateNative;
import com.knubisoft.testlum.testing.model.scenario.NavigateNativeDestination;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.ios.IOSDriver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NavigateNativeExecutorTest {

    @Mock
    private UiUtil uiUtil;
    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private ApplicationContext context;

    private NavigateNativeExecutor createExecutor(final WebDriver driver) {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies deps = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        NavigateNativeExecutor executor = new NavigateNativeExecutor(deps);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        return executor;
    }

    @Nested
    class ExecuteWithAndroid {

        @Test
        void navigatesHomeOnAndroid() {
            AndroidDriver androidDriver = mock(AndroidDriver.class);
            NavigateNativeExecutor executor = createExecutor(androidDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.HOME);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(nav, result));
            verify(androidDriver).pressKey(any());
        }

        @Test
        void navigatesBackOnAndroid() {
            AndroidDriver androidDriver = mock(AndroidDriver.class);
            NavigateNativeExecutor executor = createExecutor(androidDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.BACK);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(nav, result));
            verify(androidDriver).pressKey(any());
        }

        @Test
        void navigatesOverviewOnAndroid() {
            AndroidDriver androidDriver = mock(AndroidDriver.class);
            NavigateNativeExecutor executor = createExecutor(androidDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.OVERVIEW);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(nav, result));
            verify(androidDriver).pressKey(any());
        }

        @Test
        void putsDestinationInResult() {
            AndroidDriver androidDriver = mock(AndroidDriver.class);
            NavigateNativeExecutor executor = createExecutor(androidDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.HOME);
            CommandResult result = new CommandResult();
            executor.execute(nav, result);

            assertEquals(NavigateNativeDestination.HOME, result.getMetadata().get(ResultUtil.NATIVE_NAVIGATE_TO));
        }

        @Test
        void takesScreenshotAfterAndroidNavigation() {
            AndroidDriver androidDriver = mock(AndroidDriver.class);
            NavigateNativeExecutor executor = createExecutor(androidDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.HOME);
            CommandResult result = new CommandResult();
            executor.execute(nav, result);

            verify(uiUtil).takeScreenshotAndSaveIfRequired(eq(result), any());
        }
    }

    @Nested
    class ExecuteWithIOS {

        @Test
        void navigatesHomeOnIOS() {
            IOSDriver iosDriver = mock(IOSDriver.class);
            NavigateNativeExecutor executor = createExecutor(iosDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.HOME);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(nav, result));
            verify(iosDriver).executeScript(eq("mobile: pressButton"),
                    eq(Collections.singletonMap("name", "home")));
        }

        @Test
        void navigatesBackOnIOS() {
            IOSDriver iosDriver = mock(IOSDriver.class);
            WebDriver.Navigation navigation = mock(WebDriver.Navigation.class);
            when(iosDriver.navigate()).thenReturn(navigation);
            NavigateNativeExecutor executor = createExecutor(iosDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.BACK);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(nav, result));
            verify(navigation).back();
        }

        @Test
        void throwsForOverviewOnIOS() {
            IOSDriver iosDriver = mock(IOSDriver.class);
            NavigateNativeExecutor executor = createExecutor(iosDriver);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.OVERVIEW);
            CommandResult result = new CommandResult();
            assertThrows(DefaultFrameworkException.class, () -> executor.execute(nav, result));
        }
    }
}
