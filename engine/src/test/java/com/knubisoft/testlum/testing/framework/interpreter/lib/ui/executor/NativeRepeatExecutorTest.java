package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunner;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.framework.variations.GlobalVariations;
import com.knubisoft.testlum.testing.model.scenario.NativeRepeat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NativeRepeatExecutorTest {

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private SubCommandRunner subCommandRunner;
    @Mock
    private GlobalVariations globalVariations;
    @Mock
    private ApplicationContext context;
    @Mock
    private WebDriver driver;

    private NativeRepeatExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        when(context.getBean(SubCommandRunner.class)).thenReturn(subCommandRunner);
        when(context.getBean(GlobalVariations.class)).thenReturn(globalVariations);
        ExecutorDependencies deps = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .build();
        executor = new NativeRepeatExecutor(deps);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
    }

    @Nested
    class Execute {
        @Test
        void simpleRepeatWithZeroTimes() {
            NativeRepeat repeat = new NativeRepeat();
            repeat.setTimes(0);
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(repeat, result));
            assertNotNull(result.getSubCommandsResult());
        }

        @Test
        void simpleRepeatWithPositiveTimes() {
            NativeRepeat repeat = new NativeRepeat();
            repeat.setTimes(2);
            repeat.getClickOrInputOrAssert().addAll(new ArrayList<>());
            CommandResult result = new CommandResult();
            assertDoesNotThrow(() -> executor.execute(repeat, result));
        }
    }
}
