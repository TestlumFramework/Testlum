package com.knubisoft.testlum.testing.framework.constant;

import com.knubisoft.testlum.log.Color;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** Unit tests for {@link LogMessage} constants. */
class LogMessageTest {

    @Nested
    class UiCommandLogs {
        @Test
        void uiCommandLogContainsCyan() {
            assertTrue(LogMessage.UI_COMMAND_LOG.contains(Color.CYAN.getCode()));
        }

        @Test
        void uiCommandLogWithoutPositionContainsCyan() {
            assertTrue(LogMessage.UI_COMMAND_LOG_WITHOUT_POSITION.contains(Color.CYAN.getCode()));
        }

        @Test
        void repeatFinishedLogContainsCyan() {
            assertTrue(LogMessage.REPEAT_FINISHED_LOG.contains(Color.CYAN.getCode()));
        }

        @Test
        void commandSkippedOnConditionLogContainsOrange() {
            assertTrue(LogMessage.COMMAND_SKIPPED_ON_CONDITION_LOG.contains(Color.ORANGE.getCode()));
        }
    }

    @Nested
    class QueryLogs {
        @Test
        void queryIsNotNull() {
            assertNotNull(LogMessage.QUERY);
        }

        @Test
        void errorSqlQueryContainsRed() {
            assertTrue(LogMessage.ERROR_SQL_QUERY.contains(Color.RED.getCode()));
        }

        @Test
        void successQueryIsNotEmpty() {
            assertEquals("Query completed successfully", LogMessage.SUCCESS_QUERY);
        }
    }

    @Nested
    class ScenarioLogs {
        @Test
        void scenarioNumberAndPathLogContainsPlaceholder() {
            assertTrue(LogMessage.SCENARIO_NUMBER_AND_PATH_LOG.contains("%s"));
        }

        @Test
        void scenarioNumberAndPathLogCanBeFormatted() {
            String result = String.format(LogMessage.SCENARIO_NUMBER_AND_PATH_LOG, "#1");
            assertTrue(result.contains("#1"));
        }

        @Test
        void nonParsedScenariosTitleIsNotEmpty() {
            assertFalse(LogMessage.SKIPPED_SCENARIOS_TITLE.isEmpty());
        }

        @Test
        void invalidScenariosTitleIsNotEmpty() {
            assertFalse(LogMessage.FAILED_SCENARIOS_TITLE.isEmpty());
        }

        @Test
        void scenarioWithEmptyTagLogContainsYellow() {
            assertTrue(LogMessage.SCENARIO_WITH_EMPTY_TAG_LOG.contains(Color.YELLOW.getCode()));
        }
    }

    @Nested
    class FrameLogs {
        @Test
        void startUiCommandsInFrameContainsCyan() {
            assertTrue(LogMessage.START_UI_COMMANDS_IN_FRAME.contains(Color.CYAN.getCode()));
        }

        @Test
        void endUiCommandsInFrameContainsCyan() {
            assertTrue(LogMessage.END_UI_COMMANDS_IN_FRAME.contains(Color.CYAN.getCode()));
        }

        @Test
        void startUiCommandsInWebviewContainsCyan() {
            assertTrue(LogMessage.START_UI_COMMANDS_IN_WEBVIEW.contains(Color.CYAN.getCode()));
        }

        @Test
        void endUiCommandsInWebviewContainsCyan() {
            assertTrue(LogMessage.END_UI_COMMANDS_IN_WEBVIEW.contains(Color.CYAN.getCode()));
        }
    }

    @Nested
    class ConnectionLogs {
        @Test
        void connectionIntegrationDataContainsPlaceholders() {
            assertTrue(LogMessage.CONNECTION_INTEGRATION_DATA.contains("%s"));
        }

        @Test
        void connectingInfoContainsCyan() {
            assertTrue(LogMessage.CONNECTING_INFO.contains(Color.CYAN.getCode()));
        }

        @Test
        void connectionSuccessContainsGreen() {
            assertTrue(LogMessage.CONNECTION_SUCCESS.contains(Color.GREEN.getCode()));
        }

        @Test
        void connectionAttemptFailedContainsOrange() {
            assertTrue(LogMessage.CONNECTION_ATTEMPT_FAILED.contains(Color.ORANGE.getCode()));
        }

        @Test
        void connectionCompletelyFailedContainsRed() {
            assertTrue(LogMessage.CONNECTION_COMPLETELY_FAILED.contains(Color.RED.getCode()));
        }

        @Test
        void connectionIntegrationDataFormat() {
            String result = String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Redis", "alias-1");
            assertEquals("Redis - [alias-1]", result);
        }
    }

    @Nested
    class TableFormatLogs {
        @Test
        void dbTypeLogIsNotNull() {
            assertNotNull(LogMessage.DB_TYPE_LOG);
        }

        @Test
        void aliasLogIsNotNull() {
            assertNotNull(LogMessage.ALIAS_LOG);
        }

        @Test
        void executionTimeLogIsNotNull() {
            assertNotNull(LogMessage.EXECUTION_TIME_LOG);
        }

        @Test
        void locatorLogIsNotNull() {
            assertNotNull(LogMessage.LOCATOR_LOG);
        }

        @Test
        void valueLogIsNotNull() {
            assertNotNull(LogMessage.VALUE_LOG);
        }
    }

    @Nested
    class DisabledConfigurationLog {
        @Test
        void disabledConfigurationContainsYellow() {
            assertTrue(LogMessage.DISABLED_CONFIGURATION.contains(Color.YELLOW.getCode()));
        }

        @Test
        void executionStopSignalLogContainsYellow() {
            assertTrue(LogMessage.EXECUTION_STOP_SIGNAL_LOG.contains(Color.YELLOW.getCode()));
        }
    }

    @Nested
    class BrowserInfoLogs {
        @Test
        void browserInfoContainsPlaceholders() {
            assertTrue(LogMessage.BROWSER_INFO.contains("%s"));
        }

        @Test
        void mobileBrowserInfoContainsPlaceholders() {
            assertTrue(LogMessage.MOBILE_BROWSER_INFO.contains("%s"));
        }

        @Test
        void nativeInfoContainsPlaceholders() {
            assertTrue(LogMessage.NATIVE_INFO.contains("%s"));
        }
    }

    @Nested
    class WebsocketLogs {
        @Test
        void websocketHandlerForTopicNotFoundContainsOrange() {
            assertTrue(LogMessage.WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND.contains(Color.ORANGE.getCode()));
        }

        @Test
        void websocketAlreadySubscribedContainsOrange() {
            assertTrue(LogMessage.WEBSOCKET_ALREADY_SUBSCRIBED.contains(Color.ORANGE.getCode()));
        }

        @Test
        void websocketConnectionEstablishedIsNotEmpty() {
            assertNotNull(LogMessage.WEBSOCKET_CONNECTION_ESTABLISHED);
            assertFalse(LogMessage.WEBSOCKET_CONNECTION_ESTABLISHED.isEmpty());
        }
    }

    @Nested
    class PrivateConstructor {
        @Test
        void classIsFinal() {
            assertTrue(java.lang.reflect.Modifier.isFinal(LogMessage.class.getModifiers()));
        }
    }
}
