package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Copy;
import com.knubisoft.testlum.testing.model.scenario.Cut;
import com.knubisoft.testlum.testing.model.scenario.HotKey;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.interactions.Interactive;
import org.springframework.context.ApplicationContext;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotKeyExecutorTest {

    interface InteractiveWebDriver extends WebDriver, Interactive { }

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiUtil uiUtil;
    @Mock
    private InteractiveWebDriver driver;
    @Mock
    private ApplicationContext context;

    private HotKeyExecutor executor;

    @BeforeEach
    void setUp() {
        when(context.getBean(any(Class.class))).thenAnswer(inv -> mock((Class<?>) inv.getArgument(0)));
        ExecutorDependencies dependencies = ExecutorDependencies.builder()
                .context(context)
                .driver(driver)
                .position(new AtomicInteger(0))
                .build();
        executor = new HotKeyExecutor(dependencies);
        ReflectionTestUtils.setField(executor, "resultUtil", resultUtil);
        ReflectionTestUtils.setField(executor, "logUtil", logUtil);
        ReflectionTestUtils.setField(executor, "uiUtil", uiUtil);
    }

    @Nested
    class Execute {

        @Test
        void createsSubCommandResultsForEachHotKeyCommand() {
            HotKey hotKey = new HotKey();
            Copy copy = new Copy();
            copy.setComment("Copy command");
            hotKey.getCopyOrPasteOrCut().add(copy);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            assertNotNull(result.getSubCommandsResult());
            verify(logUtil).logHotKeyInfo(any(), anyInt());
        }
    }

    @Nested
    class CutCommand {

        @Test
        void executesCutKeyCommand() {
            HotKey hotKey = new HotKey();
            Cut cut = new Cut();
            cut.setComment("Cut command");
            hotKey.getCopyOrPasteOrCut().add(cut);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            assertNotNull(result.getSubCommandsResult());
        }
    }
}
