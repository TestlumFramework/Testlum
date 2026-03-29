package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.NavigateNative;
import com.knubisoft.testlum.testing.model.scenario.NavigateNativeDestination;
import io.appium.java_client.android.AndroidDriver;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
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

    @Nested
    class ExecuteWithAndroid {
        @Test
        void navigatesHomeOnAndroid() {
            AndroidDriver androidDriver = mock(AndroidDriver.class);
            when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
            ExecutorDependencies deps = ExecutorDependencies.builder()
                    .context(context)
                    .driver(androidDriver)
                    .build();
            NavigateNativeExecutor executor = new NavigateNativeExecutor(deps);
            ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
            ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
            ReflectionTestUtils.setField(executor, "logUtil", logUtil);

            NavigateNative nav = new NavigateNative();
            nav.setDestination(NavigateNativeDestination.HOME);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(nav, result));
            verify(androidDriver).pressKey(any());
        }
    }
}
