package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link LogUtil} verifying logging methods
 * delegate correctly and handle edge cases without errors.
 */
@ExtendWith(MockitoExtension.class)
class LogUtilTest {

    @Mock
    private BrowserUtil browserUtil;

    @Mock
    private MobileUtil mobileUtil;

    @Mock
    private StringPrettifier stringPrettifier;

    @InjectMocks
    private LogUtil logUtil;

    @Nested
    class LogCondition {
        @Test
        void logConditionTrueDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.logCondition("flag", true));
        }

        @Test
        void logConditionFalseDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.logCondition("flag", false));
        }
    }

    @Nested
    class LogConditionInfo {
        @Test
        void logsConditionInfoWithoutError() {
            assertDoesNotThrow(
                    () -> logUtil.logConditionInfo("cond", "true && false", false));
        }

        @Test
        void logsConditionInfoTrueValue() {
            assertDoesNotThrow(
                    () -> logUtil.logConditionInfo("cond", "1 == 1", true));
        }
    }

    @Nested
    class LogExecutionTime {
        @Test
        void logsNonUiCommandTime() {
            final AbstractCommand command = mock(AbstractCommand.class);
            assertDoesNotThrow(() -> logUtil.logExecutionTime(150L, command));
        }
    }

    @Nested
    class LogException {
        @Test
        void logsExceptionWithMessage() {
            final Exception ex = new RuntimeException("something failed");
            assertDoesNotThrow(() -> logUtil.logException(ex));
        }

        @Test
        void logsExceptionWithoutMessage() {
            final Exception ex = new RuntimeException();
            assertDoesNotThrow(() -> logUtil.logException(ex));
        }
    }

    @Nested
    class LogNonParsedScenarioInfo {
        @Test
        void logsNonParsedScenario() {
            assertDoesNotThrow(
                    () -> logUtil.logNonParsedScenarioInfo("/path/to/file.xml", "parse error"));
        }
    }

    @Nested
    class LogAllQueries {
        @Test
        void logsQueriesWithAlias() {
            final List<String> queries = List.of("SELECT * FROM users", "SELECT 1");
            assertDoesNotThrow(() -> logUtil.logAllQueries(queries, "myDb"));
        }

        @Test
        void logsQueriesWithDbTypeAndAlias() {
            final List<String> queries = List.of("INSERT INTO t VALUES(1)");
            assertDoesNotThrow(
                    () -> logUtil.logAllQueries("postgres", queries, "pgAlias"));
        }

        @Test
        void logsEmptyQueryList() {
            assertDoesNotThrow(() -> logUtil.logAllQueries(List.of(), "alias"));
        }
    }

    @Nested
    class LogVarInfo {
        @Test
        void logsVariableInfo() {
            when(stringPrettifier.cut("longValue")).thenReturn("longValue");
            assertDoesNotThrow(() -> logUtil.logVarInfo("myVar", "longValue"));
        }
    }

    @Nested
    class LogUICommand {
        @Test
        void logsUiCommandWithPosition() {
            final AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn(null);
            assertDoesNotThrow(() -> logUtil.logUICommand(1, command));
        }

        @Test
        void logsUiCommandWithZeroPosition() {
            final AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn(null);
            assertDoesNotThrow(() -> logUtil.logUICommand(0, command));
        }

        @Test
        void logsUiCommandWithComment() {
            final AbstractCommand command = mock(AbstractCommand.class);
            when(command.getComment()).thenReturn("my comment");
            assertDoesNotThrow(() -> logUtil.logUICommand(3, command));
        }
    }

    @Nested
    class FrameAndWebViewLogs {
        @Test
        void startUiCommandsInFrameDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.startUiCommandsInFrame());
        }

        @Test
        void endUiCommandsInFrameDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.endUiCommandsInFrame());
        }

        @Test
        void startUiCommandsInWebViewDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.startUiCommandsInWebView());
        }

        @Test
        void endUiCommandsInWebViewDoesNotThrow() {
            assertDoesNotThrow(() -> logUtil.endUiCommandsInWebView());
        }
    }

    @Nested
    class LogScenarioWithoutTags {
        @Test
        void logsWarning() {
            assertDoesNotThrow(
                    () -> logUtil.logScenarioWithoutTags("/scenarios/test/scenario.xml"));
        }
    }

    @Nested
    class LogSingleKeyCommandTimes {
        @Test
        void logsWhenTimesGreaterThanOne() {
            assertDoesNotThrow(() -> logUtil.logSingleKeyCommandTimes(3));
        }

        @Test
        void doesNotLogWhenTimesIsOne() {
            assertDoesNotThrow(() -> logUtil.logSingleKeyCommandTimes(1));
        }
    }
}
