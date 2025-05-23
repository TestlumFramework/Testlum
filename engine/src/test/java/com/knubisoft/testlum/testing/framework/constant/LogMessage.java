package com.knubisoft.testlum.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMessage {

    public static final String TABLE_FORMAT = "%-23s|%-70s";
    public static final String CONTENT_FORMAT = format("%n%19s| %-23s|", EMPTY, EMPTY);
    public static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    public static final String REGEX_NEW_LINE = "[\\r\\n]";

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001b[0m";
    public static final String ANSI_RED_BOLD = "\033[1;31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001b[36m";
    public static final String ANSI_ORANGE = "\u001b[38;5;208m";
    public static final String ANSI_BLUE_UNDERLINED = "\033[4;34m";

    public static final String ERROR_LOG = "Error ->";
    public static final String UI_COMMAND_LOG = ANSI_CYAN + "------- UI command #{} - {} -------" + ANSI_RESET;
    public static final String UI_COMMAND_LOG_WITHOUT_POSITION = ANSI_CYAN
            + "------- UI command - {} -------" + ANSI_RESET;
    public static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    public static final String REPEAT_FINISHED_LOG = ANSI_CYAN + "------- Repeat is finished -------" + ANSI_RESET;
    public static final String COMMENT_LOG = format(TABLE_FORMAT, "Comment", "{}");
    public static final String COMMAND_SKIPPED_ON_CONDITION_LOG = ANSI_ORANGE
            + "Command was skipped because of the condition" + ANSI_RESET;
    public static final String CONDITION_LOG = format(TABLE_FORMAT, "Condition", "'{}' = {};");
    public static final String ERROR_DURING_DB_MIGRATION_LOG = "Error during database migration ->";
    public static final String QUERY = format(TABLE_FORMAT, "Query", "{}");
    public static final String ERROR_SQL_QUERY = ANSI_RED + "Error while executing SQL query -> "
            + "{}" + ANSI_ORANGE + NEW_LOG_LINE + "{}" + ANSI_RESET;
    public static final String SCENARIO_NUMBER_AND_PATH_LOG = ANSI_GREEN
            + "================== Execute for scenario #{} - {} ==================" + ANSI_RESET;
    public static final String LINE =
            "-----------------------------------------------------------------------------------------------------"
                    + "--------------------------------------------------------------------------------------";
    public static final String START_UI_COMMANDS_IN_FRAME =
            ANSI_CYAN + "------- Start ui commands in frame -------" + ANSI_RESET;
    public static final String END_UI_COMMANDS_IN_FRAME =
            ANSI_CYAN + "------- End ui commands in frame -------" + ANSI_RESET;

    public static final String START_UI_COMMANDS_IN_WEBVIEW =
            ANSI_CYAN + "------- Start ui commands in web view -------" + ANSI_RESET;
    public static final String END_UI_COMMANDS_IN_WEBVIEW =
            ANSI_CYAN + "------- End ui commands in web view -------" + ANSI_RESET;
    public static final String INVALID_SCENARIO_LOG = ANSI_RED + LINE
            + NEW_LOG_LINE + format(TABLE_FORMAT, "Invalid scenario", "{}")
            + NEW_LOG_LINE + format(TABLE_FORMAT, "Exception", "{}") + NEW_LOG_LINE
            + LINE + ANSI_RESET + "\n";

    public static final String DISABLED_CONFIGURATION = ANSI_YELLOW
            + LINE + NEW_LOG_LINE
            + "<{}> configuration not found or disabled. Scenarios that depend on this config will be invalid"
            + NEW_LOG_LINE + LINE + ANSI_RESET;

    public static final String EXECUTION_STOP_SIGNAL_LOG = ANSI_YELLOW
            + LINE + NEW_LOG_LINE
            + "The execution has been stopped because of the enabled <StopScenarioOnFailure>"
            + NEW_LOG_LINE + LINE + ANSI_RESET;

    public static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
            + "--------------------------------------------------" + ANSI_RESET;

    public static final String FAILED_VARIABLE_LOG = "Failed variable <{}> comment <{}>";

    public static final String NAME_LOG = format(TABLE_FORMAT, "Name", "{}");
    public static final String BY_URL_LOG = format(TABLE_FORMAT, "URL", "{}");
    public static final String FAILED_VISITING_PATH_LOG = "Failed to visit path {}";

    public static final String WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND = ANSI_ORANGE
            + "Websocket message handler for topic <{}> not found" + ANSI_RESET;
    public static final String WEBSOCKET_ALREADY_SUBSCRIBED = ANSI_ORANGE
            + "The topic <{}> has already been subscribed to" + ANSI_RESET;
    public static final String WEBSOCKET_CONNECTION_ESTABLISHED = "Connection established: {}";
    public static final String WEBSOCKET_CONNECTION_CLOSED = "Connection closed: {}";
    public static final String UNABLE_TO_DISCONNECT_WEBSOCKET_BECAUSE_CLOSED = ANSI_ORANGE
            + "Unable to disconnect session because the connection was closed" + ANSI_RESET;

    public static final String TESTS_RUN_FAILED = "Test run failed";
    public static final String TEST_EXECUTION_SUMMARY_TEMPLATE =
            "\n\nTest run finished\n{} tests found\n{} tests skipped\n{} tests started\n{} test aborted\n"
                    + "{} test successful\n{} test failed\n";
    public static final String FAILED_SCENARIOS_NAME_TEMPLATE =
            ANSI_RED_BOLD + "Scenario %s was failed. Related exception provided below." + ANSI_RED_BOLD;
    public static final String SUCCESS_QUERY = "Query completed successfully";

    public static final String DB_TYPE_LOG = format(TABLE_FORMAT, "DB Type", "{}");
    public static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    public static final String EXECUTION_TIME_LOG = format(TABLE_FORMAT, "Execution time ", "{} ms");
    public static final String WAIT_INFO_LOG = format(TABLE_FORMAT, "Wait time & unit", "{} {}");
    public static final String LOCATOR_LOG = format(TABLE_FORMAT, "Locator", "{}");
    public static final String NEGATIVE_LOG = format(TABLE_FORMAT, "Is negative", "{}");
    public static final String UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE =
            ANSI_YELLOW + "Unable to find element by: %s. Locator was skipped" + ANSI_RESET;
    public static final String ELEMENT_WAS_FOUND_BY_LOCATOR = ANSI_GREEN + "Element was found by: {}" + ANSI_RESET;
    public static final String UNABLE_TO_FIND_ELEMENT_BY_LOCATOR =
            "No such element: The element not found using any of the provided locators. "
                    + "Please verify the locator - %s!";
    public static final String VARIATION_LOG = "Variation - {}";
    public static final String BROWSER_NAME_LOG = "Browser: {}";
    public static final String NATIVE_LOG = "Native device: {}";
    public static final String MOBILEBROWSER_LOG = "Mobilebrowser device: {}";
    public static final String NATIVE_NAVIGATION_LOG = format(TABLE_FORMAT, "Navigate to", "{}");
    public static final String VALUE_LOG = format(TABLE_FORMAT, "Value", "{}");
    public static final String EXPRESSION_LOG = format(TABLE_FORMAT, "Expression", "{}");
    public static final String SCROLL_LOCATOR = format(TABLE_FORMAT, "Scroll locator", "{}");
    public static final String SCROLL_TYPE = format(TABLE_FORMAT, "Scroll type", "{}");
    public static final String SWIPE_QUANTITY = format(TABLE_FORMAT, "Quantity of swipes", "{}");
    public static final String SWIPE_DIRECTION = format(TABLE_FORMAT, "Swipe direction", "{}");
    public static final String SWIPE_VALUE = format(TABLE_FORMAT, "Swipe value in %", "{}");
    public static final String SWIPE_TYPE = format(TABLE_FORMAT, "Swipe type", "{}");
    public static final String WAIT_TYPE = format(TABLE_FORMAT, "Wait for element to be", "{}");
    public static final String DRAGGING_FROM = format(TABLE_FORMAT, "Dragging from locator", "{}");
    public static final String DROPPING_TO = format(TABLE_FORMAT, "Dropping to locator", "{}");
    public static final String DRAGGING_FILE_PATH = format(TABLE_FORMAT, "Dragging file path", "{}");
    public static final String HOTKEY_COMMAND_LOCATOR = format(TABLE_FORMAT, "Hotkey command locator", "{}");
    public static final String HOTKEY_COMMAND_TIMES = format(TABLE_FORMAT, "Times to repeat", "{}");
    public static final String HTTP_STATUS_CODE = format(TABLE_FORMAT, "Status code", "{} {}");
    public static final String HTTP_METHOD_LOG = format(TABLE_FORMAT, "HTTP method", "{}");
    public static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    public static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    public static final String CONTENT_LOG = format(TABLE_FORMAT, "Content", "{}");
    public static final String ATTRIBUTE_LOG = format(TABLE_FORMAT, "Attribute", "{}");
    public static final String IMAGE_COMPARISON_TYPE_LOG = format(TABLE_FORMAT, "Image comparison type", "{}");
    public static final String HIGHLIGHT_DIFFERENCE_LOG = format(TABLE_FORMAT, "Highlight difference", "{}");
    public static final String IMAGE_FOR_COMPARISON_LOG = format(TABLE_FORMAT, "Image for comparison", "{}");
    public static final String IMAGE_SOURCE_ATT_LOG = format(TABLE_FORMAT, "Picture source attribute", "{}");
    public static final String IMAGE_EXCLUDED_ELEMENT_LOG = format(TABLE_FORMAT, "Excluded elements", "{}");
    public static final String IMAGE_MATCH_PERCENTAGE_LOG = format(TABLE_FORMAT, "Match percentage", "{}");
    public static final String URL_TO_IMAGE_LOG = format(TABLE_FORMAT, "URL to actual image", "{}");
    public static final String OVERVIEW_INFO_LOG = "{}: {}";
    public static final String MOVE_TO_EMPTY_SPACE = format(TABLE_FORMAT, "Move to empty space", "{}");

    public static final String UI_EXECUTION_TIME_LOG = format(TABLE_FORMAT, "Step execution time", "{} ms");
    public static final String COMMAND_TYPE_LOG = format(TABLE_FORMAT, "Command type", "{}");
    public static final String BY_LOG = format(TABLE_FORMAT, "By", "{}");
    public static final String JS_FILE_LOG = format(TABLE_FORMAT, "JS file", "{}");
    public static final String SCROLL_DIRECTION_LOG = format(TABLE_FORMAT, "Direction", "{}");
    public static final String SCROLL_BY_LOG = format(TABLE_FORMAT, "Scroll by", "{}");
    public static final String LOCATOR_STRATEGY = format(TABLE_FORMAT, "Locator strategy", "{}");
    public static final String TIMES_LOG = format(TABLE_FORMAT, "Times to repeat", "{}");
    public static final String LOCAL_STORAGE_KEY = format(TABLE_FORMAT, "Local storage key", "{}");
    public static final String CLEAR_COOKIES_AFTER = format(TABLE_FORMAT, "Clear cookies after", "{}");
    public static final String TAB_COMMAND = format(TABLE_FORMAT, "Command", "{}");
    public static final String TAB_INDEX = format(TABLE_FORMAT, "Tab Index", "{}");
    public static final String TAB_URL = format(TABLE_FORMAT, "Tab Url", "{}");

    public static final String BROWSER_INFO = "%s | type = %s | version = %s";
    public static final String MOBILEBROWSER_INFO = "Device name = %s | platform = %s | version = %s";
    public static final String MOBILEBROWSER_APPIUM_INFO = MOBILEBROWSER_INFO + " | udid = %s";
    public static final String NATIVE_INFO = "Device name = %s | platform = %s | version = %s";
    public static final String NATIVE_APPIUM_INFO = NATIVE_INFO + " | udid = %s";
    public static final String EXTRACT_THEN_COMPARE = "Extract picture from web element then compare";
    public static final String TAKE_SCREENSHOT_THEN_COMPARE = "Take a full screenshot then compare";
    public static final String GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE = "Get element as screenshot then compare";
}
