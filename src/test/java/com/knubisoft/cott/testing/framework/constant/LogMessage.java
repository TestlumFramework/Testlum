package com.knubisoft.cott.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMessage {

    public static final String TABLE_FORMAT = "%-20s|%-70s";
    public static final String CONTENT_FORMAT = format("%n%19s| %-20s|", EMPTY, EMPTY);
    public static final String NEW_LOG_LINE = format("%n%19s| ", EMPTY);
    public static final String REGEX_NEW_LINE = "[\\r\\n]";

    public static final String SEND_ACTION = "send";
    public static final String RECEIVE_ACTION = "receive";


    public static final String DESTINATION_LOG = format(TABLE_FORMAT, "Destination", "{}");
    public static final String SOURCE_LOG = format(TABLE_FORMAT, "Source", "{}");
    public static final String COMPARISON_FOR_STEP_WAS_SKIPPED = "Comparison for step [%s] was skipped";

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001b[0m";
    public static final String ANSI_RED_BOLD = "\033[1;31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001b[36m";

    public static final String ERROR_LOG = "Error ->";
    public static final String POSITION_COMMAND_LOG = ANSI_YELLOW
            + "--------- Scenario step #%d - %s ---------" + ANSI_RESET;
    public static final String UI_COMMAND_LOG = ANSI_CYAN + "------- UI command #{} - {} -------" + ANSI_RESET;
    public static final String COMMAND_LOG = ANSI_CYAN + "------- Command #{} - {} -------" + ANSI_RESET;
    public static final String REPEAT_FINISHED_LOG = ANSI_CYAN + "------- Repeat is finished -------" + ANSI_RESET;
    public static final String COMMENT_LOG = format(TABLE_FORMAT, "Comment", "{}");
    public static final String ERROR_DURING_DB_MIGRATION_LOG = "Error during database migration ->";
    public static final String SCENARIO_NUMBER_AND_PATH_LOG = ANSI_GREEN
            + "================== Execute for scenario #{} - {} ==================" + ANSI_RESET;

    public static final String EXECUTION_STOP_SIGNAL_LOG = "Execution has been stopped because of StopSignal";
    public static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------"
            + NEW_LOG_LINE + " {}" + NEW_LOG_LINE
            + "--------------------------------------------------" + ANSI_RESET;
    public static final String FAILED_VARIABLE_WITH_PATH_LOG = "Failed [variable] {} [comment] {}";

    public static final String NAME_LOG = format(TABLE_FORMAT, "Name", "{}");
    public static final String BY_URL_LOG = format(TABLE_FORMAT, "URL", "{}");
    public static final String RETHROWN_ERRORS_LOG = "Errors:%n %s";
    public static final String FAILED_VISITING_PATH_LOG = "Failed to visit path {}";

    public static final String BROKER_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Action", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT, "Queue", "{}")
            + NEW_LOG_LINE + format(TABLE_FORMAT, "Content", "{}");
    public static final String S3_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Action", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "Bucket", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "Key", "{}") + NEW_LOG_LINE + format(TABLE_FORMAT,
            "File for action", "{}");

    public static final String SES_BODY_CONTENT_AND_TITLE_TEMPLATE = "%n%46s:%n%47s%-100s";
    public static final String TEST_EXECUTION_SUMMARY_TEMPLATE =
                    "\n\nTest run finished\n{} tests found\n{} tests skipped\n{} tests started\n{} test aborted\n"
                    + "{} test successful\n{} test failed\n";
    public static final String FAILED_SCENARIOS_NAME_TEMPLATE =
                   ANSI_RED_BOLD + "Scenario %s was failed. Related exception provided below." + ANSI_RED_BOLD;
    public static final String SUCCESS_QUERY = "Query completed successfully";

    public static final String DATASET_PATH_LOG = format(TABLE_FORMAT, "Migration dataset", "{}");
    public static final String ALIAS_LOG = format(TABLE_FORMAT, "Alias", "{}");
    public static final String EXECUTION_TIME_LOG = format(TABLE_FORMAT, "Execution time ", "{} ms");
    public static final String WAIT_INFO_LOG = format(TABLE_FORMAT, "Wait time & unit", "{} {}");
    public static final String LOCATOR_LOG = format(TABLE_FORMAT, "Locator", "{}");
    public static final String VARIATION_LOG = "Variation - {}";
    public static final String BROWSER_NAME_LOG = "Browser: {}";
    public static final String HTTP_STATUS_CODE = format(TABLE_FORMAT, "Status code", "{} {}");
    public static final String VALUE_LOG = format(TABLE_FORMAT, "Value", "{}");
    public static final String HTTP_METHOD_LOG = format(TABLE_FORMAT, "Method", "{}");
    public static final String OVERVIEW_INFO_LOG = "{}: {}";

    public static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    public static final String UI_EXECUTION_TIME_LOG = format(TABLE_FORMAT, "Step execution time", "{} ms");
    public static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    public static final String COMMAND_TYPE_LOG = format(TABLE_FORMAT, "Command type", "{}");
    public static final String SHELL_FILE_LOG = format(TABLE_FORMAT, "Shell file", "{}");
    public static final String SHELL_COMMAND_LOG = format(TABLE_FORMAT, "Shell command", "{}");
    public static final String SCROLL_DIRECTION_LOG = format(TABLE_FORMAT, "Direction", "{}");
    public static final String SCROLL_BY_LOG = format(TABLE_FORMAT, "Scroll by", "{}");
    public static final String LINE =
            "-----------------------------------------------------------------------------------------------------"
                    + "--------------------------------------------------------------------------------------";
    public static final String INVALID_SCENARIO_LOG = ANSI_RED + LINE
            + NEW_LOG_LINE + format(TABLE_FORMAT, "Invalid scenario", "{}")
            + NEW_LOG_LINE + format(TABLE_FORMAT, "Exception", "{}") + NEW_LOG_LINE
            + LINE + ANSI_RESET + "\n";

    public static final String TIMES_LOG = format(TABLE_FORMAT, "Times to repeat", "{}");
    public static final String CREDENTIALS_LOG = format(TABLE_FORMAT, "Credentials", "{}");
    public static final String INVALID_CREDENTIALS_LOG = format(TABLE_FORMAT, "Invalid credentials", "{}");
    public static final String SERVER_BAD_GATEWAY_RESPONSE_LOG = format(TABLE_FORMAT, "Server is shutdown", "{}");
    public static final String SERVER_ERROR_RESPONSE_LOG = format(TABLE_FORMAT, "Request failed", "{}");
    public static final String LOCAL_STORAGE_KEY = format(TABLE_FORMAT, "Local storage key", "{}");
    public static final String CLEAR_COOKIES_AFTER = format(TABLE_FORMAT, "Clear cookies after", "{}");

    public static final String JS_FILE_LOG = format(TABLE_FORMAT, "JS file", "{}");

    public static final String BROWSER_INFO = "%s | type = %s | version = %s";

}
