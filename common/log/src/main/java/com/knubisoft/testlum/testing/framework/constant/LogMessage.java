package com.knubisoft.testlum.testing.framework.constant;

import com.knubisoft.testlum.log.LogFormat;

public final class LogMessage {

    public static final String UI_COMMAND_LOG =
            LogFormat.withCyan("------- UI command #{} - {} -------");
    public static final String UI_COMMAND_LOG_WITHOUT_POSITION =
            LogFormat.withCyan("------- UI command - {} -------");
    public static final String REPEAT_FINISHED_LOG =
            LogFormat.withCyan("------- Repeat is finished -------");
    public static final String COMMENT_LOG =
            LogFormat.table("Comment");
    public static final String COMMAND_SKIPPED_ON_CONDITION_LOG =
            LogFormat.withOrange("Command was skipped because of the condition ");
    public static final String CONDITION_LOG =
            LogFormat.table("Condition", "'{}' = {};");

    public static final String QUERY = LogFormat.table("Query");
    public static final String ERROR_SQL_QUERY = LogFormat.withRed(
            "Error while executing SQL query -> {}" + LogFormat.newLogLine() + "{}");
    public static final String SCENARIO_NUMBER_AND_PATH_LOG =
            "================== Execute for scenario %s ==================";

    public static final String LINE =
            "-----------------------------------------------------------------------------------------------------";

    public static final String START_UI_COMMANDS_IN_FRAME =
            LogFormat.withCyan("------- Start ui commands in frame -------");
    public static final String END_UI_COMMANDS_IN_FRAME =
            LogFormat.withCyan("------- End ui commands in frame -------");

    public static final String START_UI_COMMANDS_IN_WEBVIEW =
            LogFormat.withCyan("------- Start ui commands in web view -------");
    public static final String END_UI_COMMANDS_IN_WEBVIEW =
            LogFormat.withCyan("------- End ui commands in web view -------");

    public static final String INVALID_SCENARIO_LOG = LogFormat.withRed(LINE
            + LogFormat.newLogLine() + LogFormat.table("Invalid scenario")
            + LogFormat.newLogLine() + LogFormat.table("Exception") + LogFormat.newLogLine()
            + LINE) + "\n";

    public static final String SCENARIO_WITH_EMPTY_TAG_LOG = LogFormat.withYellow(LINE
            + LogFormat.newLogLine()
            + LogFormat.table("Scenario was skipped because of empty tag")
            + LogFormat.newLogLine()
            + LINE) + "\n";

    public static final String DISABLED_CONFIGURATION = LogFormat.withYellow(LINE
            + LogFormat.newLogLine()
            + "<{}> configuration not found or disabled. Scenarios that depend on this config will be invalid"
            + LogFormat.newLogLine()
            + LINE
    );

    public static final String EXECUTION_STOP_SIGNAL_LOG = LogFormat.withYellow(LINE
            + LogFormat.newLogLine()
            + "The execution has been stopped because of the enabled <StopScenarioOnFailure>"
            + LogFormat.newLogLine()
            + LINE);

    public static final String FAILED_VARIABLE_LOG = "Failed variable <{}> comment <{}>";

    public static final String NAME_LOG = LogFormat.table("Name");
    public static final String BY_URL_LOG = LogFormat.table("URL");
    public static final String FAILED_VISITING_PATH_LOG = "Failed to visit path {}";

    public static final String WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND =
            LogFormat.withOrange("Websocket message handler for topic <{}> not found");
    public static final String WEBSOCKET_ALREADY_SUBSCRIBED =
            LogFormat.withOrange("The topic <{}> has already been subscribed to");
    public static final String WEBSOCKET_CONNECTION_ESTABLISHED = "Connection established: {}";
    public static final String WEBSOCKET_CONNECTION_CLOSED = "Connection closed: {}";
    public static final String UNABLE_TO_DISCONNECT_WEBSOCKET_BECAUSE_CLOSED =
            LogFormat.withOrange("Unable to disconnect session because the connection was closed");

    public static final String SUCCESS_QUERY = "Query completed successfully";

    public static final String DB_TYPE_LOG = LogFormat.table("DB Type", "{}");
    public static final String ALIAS_LOG = LogFormat.table("Alias", "{}");
    public static final String EXECUTION_TIME_LOG = LogFormat.table("Execution time ", "{} ms");
    public static final String WAIT_INFO_LOG = LogFormat.table("Wait time & unit", "{} {}");
    public static final String LOCATOR_LOG = LogFormat.table("Locator");
    public static final String NEGATIVE_LOG = LogFormat.table("Is negative");
    public static final String UNABLE_TO_FIND_ELEMENT_BY_LOCATOR_TYPE =
            LogFormat.withYellow("Unable to find element by: %s. Locator was skipped");
    public static final String ELEMENT_WAS_FOUND_BY_LOCATOR =
            LogFormat.withGreen("Element was found by: {}");

    public static final String UNABLE_TO_FIND_ELEMENT_BY_LOCATOR =
            "No such element: The element not found using any of the provided locators. "
                    + "Please verify the locator - %s!";
    public static final String VARIATION_LOG = "Variation - %s";
    public static final String BROWSER_NAME_LOG = "Browser: %s";
    public static final String NATIVE_LOG = "Native device: %s";
    public static final String MOBILE_BROWSER_LOG = "Mobile browser device: %s";

    public static final String NATIVE_NAVIGATION_LOG = LogFormat.table("Navigate to");
    public static final String VALUE_LOG = LogFormat.table("Value");
    public static final String EXPRESSION_LOG = LogFormat.table("Expression");
    public static final String SCROLL_LOCATOR = LogFormat.table("Scroll locator");
    public static final String SCROLL_TYPE = LogFormat.table("Scroll type");
    public static final String SWIPE_QUANTITY = LogFormat.table("Quantity of swipes");
    public static final String SWIPE_DIRECTION = LogFormat.table("Swipe direction");
    public static final String SWIPE_VALUE = LogFormat.table("Swipe value in %");
    public static final String SWIPE_TYPE = LogFormat.table("Swipe type");
    public static final String WAIT_TYPE = LogFormat.table("Wait for element to be");
    public static final String DRAGGING_FROM = LogFormat.table("Dragging from locator");
    public static final String DROPPING_TO = LogFormat.table("Dropping to locator");
    public static final String DRAGGING_FILE_PATH = LogFormat.table("Dragging file path");
    public static final String HOTKEY_COMMAND_LOCATOR = LogFormat.table("Hotkey command locator");
    public static final String HOTKEY_COMMAND_TIMES = LogFormat.table("Times to repeat");

    public static final String CONTENT_LOG = LogFormat.table("Content");
    public static final String ATTRIBUTE_LOG = LogFormat.table("Attribute");
    public static final String IMAGE_COMPARISON_TYPE_LOG = LogFormat.table("Image comparison type");
    public static final String HIGHLIGHT_DIFFERENCE_LOG = LogFormat.table("Highlight difference");
    public static final String IMAGE_FOR_COMPARISON_LOG = LogFormat.table("Image for comparison");
    public static final String IMAGE_SOURCE_ATT_LOG = LogFormat.table("Picture source attribute");
    public static final String IMAGE_EXCLUDED_ELEMENT_LOG = LogFormat.table("Excluded elements");
    public static final String IMAGE_MATCH_PERCENTAGE_LOG = LogFormat.table("Match percentage");
    public static final String URL_TO_IMAGE_LOG = LogFormat.table("URL to actual image");
    public static final String OVERVIEW_INFO_LOG = "%s: %s";
    public static final String MOVE_TO_EMPTY_SPACE = LogFormat.table("Move to empty space");

    public static final String UI_EXECUTION_TIME_LOG = LogFormat.table("Step execution time", "{} ms");
    public static final String COMMAND_TYPE_LOG = LogFormat.table("Command type");
    public static final String BY_LOG = LogFormat.table("By");
    public static final String JS_FILE_LOG = LogFormat.table("JS file");
    public static final String SCROLL_DIRECTION_LOG = LogFormat.table("Direction");
    public static final String SCROLL_BY_LOG = LogFormat.table("Scroll by");
    public static final String LOCATOR_STRATEGY = LogFormat.table("Locator strategy");

    public static final String TAB_COMMAND = LogFormat.table("Command");
    public static final String TAB_INDEX = LogFormat.table("Tab Index");
    public static final String TAB_URL = LogFormat.table("Tab Url");

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
            LogFormat.withCyan("Connecting to {} (Attempt {}/{})");
    public static final String CONNECTION_SUCCESS =
            LogFormat.withGreen("Successfully connected to {}");
    public static final String CONNECTION_ATTEMPT_FAILED =
            LogFormat.withOrange("Attempt {} failed for {} with error: {}");
    public static final String CONNECTION_ATTEMPT_RETRYING =
            "Retrying to connect to {} in {}ms";
    public static final String CONNECTION_COMPLETELY_FAILED =
            LogFormat.withRed("Max attempts reached for {}. Failed to obtain connection with cause {}");
    public static final String START_HEAL_LOG =
            LogFormat.withYellow("Unable to find element by provided locators. Start healing process...");
    public static final String HEAL_RESULT_LOG =
            LogFormat.withGreen("Element was healed successfully. New values for locator {} were generated in {}");

    public static final String SKIPPED_SCENARIOS_TITLE = "Skipped scenarios (not configured to run)";
    public static final String FAILED_SCENARIOS_TITLE = "Failed scenarios (expected to run but could not)";
    public static final String INVALID_SCENARIOS_SUMMARY = LogFormat.withRed(
            "{} scenario(s) were expected to run but failed to load. See the error table above for details."
    );

    public static final String UI_CONFIG_TABLE_TITLE = "UI Configuration";
    public static final String UI_CONFIG_TABLE_ENV_ROW = "Environment: %s";
    public static final String UI_CONFIG_TABLE_WEB_ROW = "Web";
    public static final String UI_CONFIG_TABLE_NATIVE_ROW = "Native";
    public static final String UI_CONFIG_TABLE_MOBILE_BROWSER_ROW = "Mobile Browser";
    public static final String UI_CONFIG_TABLE_BASE_URL_HEADER = "Base URL";
    public static final String UI_CONFIG_TABLE_ENABLED_HEADER = "Enabled";
    public static final String UI_CONFIG_TABLE_BROWSER_ALIAS_HEADER = "Aliases";
    public static final String UI_CONFIG_TABLE_BROWSER_TYPE_HEADER = "Browser Types";
    public static final String UI_CONFIG_TABLE_DEVICE_ALIAS_HEADER = "Device Aliases";
    public static final String UI_CONFIG_TABLE_CONNECTION_TYPE_HEADER = "Connection Type";
    public static final String UI_CONFIG_TABLE_NATIVE_PLATFORM_HEADER = "Platform";
    public static final String CONNECTION_APPIUM_SERVER = "Appium Server";
    public static final String CONNECTION_BROWSER_STACK = "BrowserStack";
    public static final String EMPTY_DELIMITER = " ";

    public static final String INTEGRATION_CONFIG_TABLE_TITLE = "Integrations Configuration";
    public static final String INTEGRATION_CONFIG_TABLE_ENV_ROW = "Environment: %s";
    public static final String INTEGRATION_CONFIG_TABLE_NAME_HEADER = "Name";
    public static final String INTEGRATION_CONFIG_TABLE_ALIAS_HEADER = "Alias";
    public static final String INTEGRATION_CONFIG_TABLE_ENABLED_HEADER = "Enabled";

    public static final String UI_ELEMENT_DISABLED_EXCEPTION_MESSAGE =
            "Element is disabled and cannot accept interactions. Check if the '%s' attribute is set on the element";
    public static final String UI_ELEMENT_IS_NOT_INTERACTABLE_EXCEPTION_MESSAGE =
            "Element is not interactable: it is covered by another element such as an overlay, modal, or tooltip";
    public static final String UI_ELEMENT_IS_NOT_VISIBLE_EXCEPTION_MESSAGE =
            "Element is not visible: it may have 'display:none' or 'visibility:hidden' applied";
    public static final String UI_ELEMENT_HAS_ZERO_SIZE_EXCEPTION_MESSAGE =
            "Element has zero dimensions and cannot be interacted with";

    private LogMessage() {
        // nop
    }

}
