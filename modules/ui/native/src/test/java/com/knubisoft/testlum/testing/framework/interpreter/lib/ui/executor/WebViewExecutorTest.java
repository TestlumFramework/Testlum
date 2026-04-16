package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.WebView;
import io.appium.java_client.remote.SupportsContextSwitching;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebViewExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private SubCommandRunnerImpl subCommandRunner;
    @Mock
    private ApplicationContext context;

    @Nested
    class Execute {
        @Test
        void switchesToWebViewAndBack() {
            // Create a mock that implements both AppiumDriver and SupportsContextSwitching
            TestDriverWithContextSwitching driver = mock(TestDriverWithContextSwitching.class);
            when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
            when(context.getBean(SubCommandRunnerImpl.class)).thenReturn(subCommandRunner);
            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(context)
                    .driver(driver)
                    .build();
            WebViewExecutor executor = new WebViewExecutor(deps);
            ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
            ReflectionTestUtils.setField(executor, "logUtil", logUtil);

            when(driver.getContextHandles()).thenReturn(Set.of("NATIVE_APP", "WEBVIEW_1"));

            WebView webView = new WebView();
            webView.getClickOrInputOrAssert().addAll(new ArrayList<>());
            CommandResult result = new CommandResult();

            assertDoesNotThrow(() -> executor.execute(webView, result));
            verify(driver).context("WEBVIEW_1");
            verify(driver).context("NATIVE_APP");
        }
    }

    private interface TestDriverWithContextSwitching
            extends org.openqa.selenium.WebDriver, SupportsContextSwitching {
    }
}
