package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.UiLogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.BackSpace;
import com.knubisoft.testlum.testing.model.scenario.Copy;
import com.knubisoft.testlum.testing.model.scenario.Cut;
import com.knubisoft.testlum.testing.model.scenario.Enter;
import com.knubisoft.testlum.testing.model.scenario.Escape;
import com.knubisoft.testlum.testing.model.scenario.Highlight;
import com.knubisoft.testlum.testing.model.scenario.HotKey;
import com.knubisoft.testlum.testing.model.scenario.Paste;
import com.knubisoft.testlum.testing.model.scenario.Space;
import com.knubisoft.testlum.testing.model.scenario.Tab;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HotKeyExecutorTest {

    interface InteractiveWebDriver extends WebDriver, Interactive { }

    @Mock
    private ResultUtil resultUtil;
    @Mock
    private LogUtil logUtil;
    @Mock
    private UiLogUtil uiLogUtil;
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
        ReflectionTestUtils.setField(executor, "uiLogUtil", uiLogUtil);
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
            verify(uiLogUtil).logHotKeyInfo(any(), anyInt());
        }

        @Test
        void createsSubResultsForMultipleCommands() {
            HotKey hotKey = new HotKey();
            Copy copy = new Copy();
            Cut cut = new Cut();
            hotKey.getCopyOrPasteOrCut().add(copy);
            hotKey.getCopyOrPasteOrCut().add(cut);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            assertEquals(2, result.getSubCommandsResult().size());
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

    @Nested
    class CopyCommand {

        @Test
        void executesCopyKeyCommand() {
            HotKey hotKey = new HotKey();
            Copy copy = new Copy();
            hotKey.getCopyOrPasteOrCut().add(copy);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            assertNotNull(result.getSubCommandsResult());
            assertEquals(1, result.getSubCommandsResult().size());
        }
    }

    @Nested
    class PasteCommand {

        @Test
        void executesPasteWithLocator() {
            HotKey hotKey = new HotKey();
            Paste paste = new Paste();
            paste.setLocator("input-field");
            hotKey.getCopyOrPasteOrCut().add(paste);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("input-field"), any())).thenReturn(element);

            executor.execute(hotKey, result);

            assertNotNull(result.getSubCommandsResult());
            verify(uiUtil).findWebElement(any(), eq("input-field"), any());
        }

        @Test
        void executesPasteWithoutLocatorUsesActiveElement() {
            HotKey hotKey = new HotKey();
            Paste paste = new Paste();
            hotKey.getCopyOrPasteOrCut().add(paste);
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
            when(driver.switchTo()).thenReturn(targetLocator);
            WebElement activeElement = mock(WebElement.class);
            when(targetLocator.activeElement()).thenReturn(activeElement);

            CommandResult result = new CommandResult();
            executor.execute(hotKey, result);

            verify(driver.switchTo()).activeElement();
        }
    }

    @Nested
    class HighlightCommand {

        @Test
        void executesHighlightWithLocator() {
            HotKey hotKey = new HotKey();
            Highlight highlight = new Highlight();
            highlight.setLocator("text-area");
            hotKey.getCopyOrPasteOrCut().add(highlight);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            WebElement element = mock(WebElement.class);
            when(uiUtil.findWebElement(any(), eq("text-area"), any())).thenReturn(element);

            executor.execute(hotKey, result);

            verify(uiUtil).findWebElement(any(), eq("text-area"), any());
        }

        @Test
        void executesHighlightWithoutLocatorUsesActiveElement() {
            HotKey hotKey = new HotKey();
            Highlight highlight = new Highlight();
            hotKey.getCopyOrPasteOrCut().add(highlight);
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);
            WebDriver.TargetLocator targetLocator = mock(WebDriver.TargetLocator.class);
            when(driver.switchTo()).thenReturn(targetLocator);
            WebElement activeElement = mock(WebElement.class);
            when(targetLocator.activeElement()).thenReturn(activeElement);

            CommandResult result = new CommandResult();
            executor.execute(hotKey, result);

            verify(driver.switchTo()).activeElement();
        }
    }

    @Nested
    class SingleKeyCommands {

        @Test
        void executesTabKeyCommand() {
            HotKey hotKey = new HotKey();
            Tab tab = new Tab();
            tab.setTimes(2);
            hotKey.getCopyOrPasteOrCut().add(tab);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            verify(resultUtil).addSingleKeyCommandMetaData(eq(2), eq(subResult));
            verify(uiLogUtil).logSingleKeyCommandTimes(2);
        }

        @Test
        void executesEnterKeyCommand() {
            HotKey hotKey = new HotKey();
            Enter enter = new Enter();
            enter.setTimes(1);
            hotKey.getCopyOrPasteOrCut().add(enter);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            verify(resultUtil).addSingleKeyCommandMetaData(eq(1), eq(subResult));
        }

        @Test
        void executesBackSpaceKeyCommand() {
            HotKey hotKey = new HotKey();
            BackSpace backSpace = new BackSpace();
            backSpace.setTimes(3);
            hotKey.getCopyOrPasteOrCut().add(backSpace);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            verify(resultUtil).addSingleKeyCommandMetaData(eq(3), eq(subResult));
        }

        @Test
        void executesEscapeKeyCommand() {
            HotKey hotKey = new HotKey();
            Escape escape = new Escape();
            escape.setTimes(1);
            hotKey.getCopyOrPasteOrCut().add(escape);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            verify(resultUtil).addSingleKeyCommandMetaData(eq(1), eq(subResult));
        }

        @Test
        void executesSpaceKeyCommand() {
            HotKey hotKey = new HotKey();
            Space space = new Space();
            space.setTimes(2);
            hotKey.getCopyOrPasteOrCut().add(space);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            verify(resultUtil).addSingleKeyCommandMetaData(eq(2), eq(subResult));
        }

        @Test
        void defaultTimesIsOneWhenNotSet() {
            HotKey hotKey = new HotKey();
            Tab tab = new Tab();
            hotKey.getCopyOrPasteOrCut().add(tab);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            executor.execute(hotKey, result);

            verify(resultUtil).addSingleKeyCommandMetaData(eq(1), eq(subResult));
        }
    }

    @Nested
    class UnsupportedCommand {

        @Test
        void throwsForUnsupportedHotKeyCommand() {
            HotKey hotKey = new HotKey();
            com.knubisoft.testlum.testing.model.scenario.Navigate unsupported =
                    new com.knubisoft.testlum.testing.model.scenario.Navigate();
            hotKey.getCopyOrPasteOrCut().add(unsupported);
            CommandResult result = new CommandResult();
            CommandResult subResult = new CommandResult();
            when(resultUtil.newUiCommandResultInstance(anyInt(), any())).thenReturn(subResult);

            assertThrows(DefaultFrameworkException.class, () -> executor.execute(hotKey, result));
        }
    }
}
