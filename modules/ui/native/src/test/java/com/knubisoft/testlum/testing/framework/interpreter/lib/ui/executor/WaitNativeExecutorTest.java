package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import com.knubisoft.testlum.testing.model.scenario.WaitNative;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WaitNativeExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private WaitUtil waitUtil;
    @Mock
    private WebDriver driver;
    @Mock
    private ApplicationContext context;

    private WaitNativeExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(context.getBean(WaitUtil.class)).thenReturn(waitUtil);
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new WaitNativeExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
    }

    @Nested
    class Execute {
        @Test
        void sleepsForSpecifiedTime() {
            WaitNative waitNative = new WaitNative();
            waitNative.setTime("500");
            waitNative.setUnit(Timeunit.MILLIS);
            when(waitUtil.getTimeUnit(Timeunit.MILLIS)).thenReturn(TimeUnit.MILLISECONDS);
            doNothing().when(resultUtil).addWaitMetaData(anyString(), any(TimeUnit.class), any());
            doNothing().when(waitUtil).sleep(anyLong(), any(TimeUnit.class));
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(waitNative, result));
            verify(waitUtil).sleep(500L, TimeUnit.MILLISECONDS);
        }
    }
}
