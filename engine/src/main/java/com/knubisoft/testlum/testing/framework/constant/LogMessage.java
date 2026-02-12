package com.knubisoft.testlum.testing.framework.constant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

public final class LogMessage {

    public static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    public static final String REGEX_NEW_LINE = "[\\r\\n]";

    public static final String UI_COMMAND_LOG =
            withCyan("------- UI command #{} - {} -------");
    public static final String UI_COMMAND_LOG_WITHOUT_POSITION =
            withCyan("------- UI command - {} -------");
    public static final String COMMAND_LOG =
            withCyan("------- Command #{} - {} -------");
    public static final String REPEAT_FINISHED_LOG =
            withCyan("------- Repeat is finished -------");
    public static final String COMMENT_LOG =
            table("Comment");
    public static final String COMMAND_SKIPPED_ON_CONDITION_LOG =
            withOrange("Command was skipped because of the condition ");
    public static final String CONDITION_LOG =
            table("Condition", "'{}' = {};");

    public static final String QUERY = table("Query");
    public static final String ERROR_SQL_QUERY = withRed(
            "Error while executing SQL query -> {}" + NEW_LOG_LINE + "{}");
    public static final String SCENARIO_NUMBER_AND_PATH_LOG =
            "================== Execute for scenario %s ==================";
    public static final String LINE =
            "-----------------------------------------------------------------------------------------------------";
    public static final String START_UI_COMMANDS_IN_FRAME =
            withCyan("------- Start ui commands in frame -------");
    public static final String END_UI_COMMANDS_IN_FRAME =
            withCyan("------- End ui commands in frame -------");

    public static final String START_UI_COMMANDS_IN_WEBVIEW =
            withCyan("------- Start ui commands in web view -------");
    public static final String END_UI_COMMANDS_IN_WEBVIEW =
            withCyan("------- End ui commands in web view -------");

    public static final String INVALID_SCENARIO_LOG = withRed(LINE
            + NEW_LOG_LINE + table("Invalid scenario")
            + NEW_LOG_LINE + table("Exception") + NEW_LOG_LINE
            + LINE) + "\n";

    public static final String SCENARIO_WITH_EMPTY_TAG_LOG = withYellow(
            LINE
                    + NEW_LOG_LINE
                    + table("Scenario was skipped because of empty tag")
                    + NEW_LOG_LINE
                    + LINE) + "\n";

    public static final String DISABLED_CONFIGURATION = withYellow(
            LINE
                    + NEW_LOG_LINE
                    + "<{}> configuration not found or disabled. Scenarios that depend on this config will be invalid"
                    + NEW_LOG_LINE
                    + LINE
    );

    public static final String EXECUTION_STOP_SIGNAL_LOG = withYellow(
            LINE
                    + NEW_LOG_LINE
                    + "The execution has been stopped because of the enabled <StopScenarioOnFailure>"
                    + NEW_LOG_LINE
                    + LINE);

    public static final String EXCEPTION_LOG = withRed(
            "----------------    EXCEPTION    -----------------"
                    + NEW_LOG_LINE + "{}" + NEW_LOG_LINE
                    + "--------------------------------------------------");

    public static final String FAILED_VARIABLE_LOG = "Failed variable <{}> comment <{}>";

    public static final String NAME_LOG = table("Name");
    public static final String BY_URL_LOG = table("URL");
    public static final String FAILED_VISITING_PATH_LOG = "Failed to visit path {}";

    public static final String WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND =
            withOrange("Websocket message handler for topic <{}> not found");
    public static final String WEBSOCKET_ALREADY_SUBSCRIBED =
            withOrange("The topic <{}> has already been subscribed to");
    public static final String WEBSOCKET_CONNECTION_ESTABLISHED = "Connection established: {}";
    public static final String WEBSOCKET_CONNECTION_CLOSED = "Connection closed: {}";
    public static final String UNABLE_TO_DISCONNECT_WEBSOCKET_BECAUSE_CLOSED =
            withOrange("Unable to disconnect session because the connection was closed");

    public static final String SUCCESS_QUERY = "Query completed successfully";

    public static final String DB_TYPE_LOG = table("DB Type", "{}");
    public static final String ALIAS_LOG = table("Alias", "{}");
    public static final String EXECUTION_TIME_LOG = table("Execution time ", "{} ms");
    public static final String WAIT_INFO_LOG = table("Wait time & unit", "{} {}");
    public static final String LOCATOR_LOG = table("Locator");
    public static final String NEGATIVE_LOG = table("Is negative");
    public static final String UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE =
            withYellow("Unable to find element by: %s. Locator was skipped");
    public static final String ELEMENT_WAS_FOUND_BY_LOCATOR =
            withGreen("Element was found by: {}");

    public static final String UNABLE_TO_FIND_ELEMENT_BY_LOCATOR =
            "No such element: The element not found using any of the provided locators. "
                    + "Please verify the locator - %s!";
    public static final String VARIATION_LOG = "Variation - %s";
    public static final String BROWSER_NAME_LOG = "Browser: %s";
    public static final String NATIVE_LOG = "Native device: %s";
    public static final String MOBILE_BROWSER_LOG = "Mobile browser device: %s";

    public static final String NATIVE_NAVIGATION_LOG = table("Navigate to");
    public static final String VALUE_LOG = table("Value");
    public static final String EXPRESSION_LOG = table("Expression");
    public static final String SCROLL_LOCATOR = table("Scroll locator");
    public static final String SCROLL_TYPE = table("Scroll type");
    public static final String SWIPE_QUANTITY = table("Quantity of swipes");
    public static final String SWIPE_DIRECTION = table("Swipe direction");
    public static final String SWIPE_VALUE = table("Swipe value in %");
    public static final String SWIPE_TYPE = table("Swipe type");
    public static final String WAIT_TYPE = table("Wait for element to be");
    public static final String DRAGGING_FROM = table("Dragging from locator");
    public static final String DROPPING_TO = table("Dropping to locator");
    public static final String DRAGGING_FILE_PATH = table("Dragging file path");
    public static final String HOTKEY_COMMAND_LOCATOR = table("Hotkey command locator");
    public static final String HOTKEY_COMMAND_TIMES = table("Times to repeat");
    public static final String HTTP_STATUS_CODE = table("Status code", "{} {}");

    public static final String CONTENT_LOG = table("Content");
    public static final String ATTRIBUTE_LOG = table("Attribute");
    public static final String IMAGE_COMPARISON_TYPE_LOG = table("Image comparison type");
    public static final String HIGHLIGHT_DIFFERENCE_LOG = table("Highlight difference");
    public static final String IMAGE_FOR_COMPARISON_LOG = table("Image for comparison");
    public static final String IMAGE_SOURCE_ATT_LOG = table("Picture source attribute");
    public static final String IMAGE_EXCLUDED_ELEMENT_LOG = table("Excluded elements");
    public static final String IMAGE_MATCH_PERCENTAGE_LOG = table("Match percentage");
    public static final String URL_TO_IMAGE_LOG = table("URL to actual image");
    public static final String OVERVIEW_INFO_LOG = "%s: %s";
    public static final String MOVE_TO_EMPTY_SPACE = table("Move to empty space");

    public static final String UI_EXECUTION_TIME_LOG = table("Step execution time", "{} ms");
    public static final String COMMAND_TYPE_LOG = table("Command type");
    public static final String BY_LOG = table("By");
    public static final String JS_FILE_LOG = table("JS file");
    public static final String SCROLL_DIRECTION_LOG = table("Direction");
    public static final String SCROLL_BY_LOG = table("Scroll by");
    public static final String LOCATOR_STRATEGY = table("Locator strategy");

    public static final String LOCAL_STORAGE_KEY = table("Local storage key");
    public static final String CLEAR_COOKIES_AFTER = table("Clear cookies after");
    public static final String TAB_COMMAND = table("Command");
    public static final String TAB_INDEX = table("Tab Index");
    public static final String TAB_URL = table("Tab Url");

    public static final String BROWSER_INFO =
            "%s | type = %s | version = %s";
    public static final String MOBILE_BROWSER_INFO =
            "Device name = %s | platform = %s | version = %s";
    public static final String MOBILE_BROWSER_APPIUM_INFO =
            MOBILE_BROWSER_INFO + " | udid = %s";
    public static final String NATIVE_INFO =
            "Device name = %s | platform = %s | version = %s";
    public static final String NATIVE_APPIUM_INFO =
            NATIVE_INFO + " | udid = %s";
    public static final String EXTRACT_THEN_COMPARE =
            "Extract picture from web element then compare";
    public static final String TAKE_SCREENSHOT_THEN_COMPARE =
            "Take a full screenshot then compare";
    public static final String GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE =
            "Get element as screenshot then compare";

    public static final String CONNECTION_INTEGRATION_DATA = "%s - [%s]";
    public static final String CONNECTING_INFO =
            withCyan("Connecting to {} (Attempt {}/{})");
    public static final String CONNECTION_SUCCESS =
            withGreen("Successfully connected to {}");
    public static final String CONNECTION_ATTEMPT_FAILED =
            withOrange("Attempt {} failed for {} with error: {}");
    public static final String CONNECTION_ATTEMPT_RETRYING =
            "Retrying to connect to {} in {}ms";
    public static final String CONNECTION_COMPLETELY_FAILED =
            withRed("Max attempts reached for {}. Failed to obtain connection with cause {}");

    private LogMessage() {
        // nop
    }

    public static String table(String text, String arg) {
        return String.format("%-23s|%-70s", text, arg);
    }

    public static String table(String text) {
        return table(text, "{}");
    }

    public static String with(Color c, String text) {
        return c.getCode() + text + Color.RESET.getCode();
    }

    public static String withRed(String text) {
        return with(Color.RED, text);
    }

    public static String withOrange(String text) {
        return with(Color.ORANGE, text);
    }

    public static String withGreen(String text) {
        return with(Color.GREEN, text);
    }

    public static String withCyan(String text) {
        return with(Color.CYAN, text);
    }

    public static String withYellow(String text) {
        return with(Color.YELLOW, text);
    }
}
