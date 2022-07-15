package com.knubisoft.e2e.testing.framework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMessage {

    public static final String TABLE_FORMAT = "%-20s|%-70s";
    public static final String CONTENT_FORMAT = format("%n%-41s|", EMPTY);

    public static final String REGEX_NEW_LINE = "[\\r\\n]";

    public static final String SEND_ACTION = "send";
    public static final String RECEIVE_ACTION = "receive";

    public static final String HTTP_CODE_EXPECTED_BUT_WAS = " Http code should be [%s] but was [%s]";
    public static final String HTTP_HEADERS_EXPECTED_BUT_WAS = " Http headers should be [%s] but was [%s]";
    public static final String HTTP_BODY_EXPECTED_BUT_WAS = " Http body should be [%s] but was [%s]";
    public static final String INCORRECT_HTTP_PROCESSING = "Incorrect http processing";
    public static final String INCORRECT_S3_PROCESSING = "Incorrect S3 processing";
    public static final String INCORRECT_SQS_PROCESSING = "Incorrect SQS processing";
    public static final String DESTINATION_LOG = format(TABLE_FORMAT, "Destination", "{}");
    public static final String SOURCE_LOG = format(TABLE_FORMAT, "Source", "{}");
    public static final String COMPARISON_FOR_STEP_WAS_SKIPPED = "Comparison for step [%s] was skipped";
    public static final String NAME_FOR_MIGRATION_MUST_PRESENT = "Data storage name for migration must present";
    public static final String SLOW_COMMAND_PROCESSING = "Slow command processing detected. "
            + "Took %d ms, threshold %d ms";

    public static final String ALIAS_BY_STORAGE_NAME_NOT_FOUND = "Alias for data storage [%s] not found. "
            + "Available aliases are %s";

    public static final String SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF = "Scenario cannot be included to itself";

    public static final String NOT_DECLARED_WITH_INTERPRETER_FOR_CLASS = "Each non abstract interpreter must "
            + "declare annotation InterpreterForClass. Class %s do not have this annotation";

    public static final String UNKNOWN_TYPE = "Unknown type %s";

    public static final String UNKNOWN_OPERATION = "Unknown operation for invoke object";

    public static final String FUNCTION_IS_NOT_INVOKE_SUPPLIER =
            "Class-function should be an instance from InvokeSupplier";
    public static final String FAILED_CONNECTION_TO_DATABASE =
            "Connection to database with name \"%s\" failed. "
                    + "Please check global configuration, it may be disabled or doesn't exist";

    public static final String FUNCTION_FOR_COMMAND_NOT_FOUND = "Function for class %s not found. "
            + "Please register algorithm for interpreter";

    public static final String UNABLE_FIND_FILE_IN_ROOT_FOLDER = "Unable to find file by key [%1$s] "
            + "Initial scan folder [%2$s] with strategy recursive walk to root folder [%3$s]";

    public static final String XSDISSUE_TO_STRING =
            "XSDIssue{message='%s', lineNumber=%d, columnNumber=%d, path=%s}";

    public static final String INCORRECT_NAMING_FOR_LOCATOR_ID =
            "Incorrect naming for %s. Expect format: <pageName.locatorId>";

    public static final String UNABLE_TO_FIND_LOCATOR_BY_PATH = "Unable to find locator [%s] by path";
    public static final String UNABLE_PARSE_FILE_WITH_LOCATORS = "Unable to parse file %s with locators. Error: %s";
    public static final String MISSING_CONSTRUCTOR = "Missing constructor for class %s";
    public static final String UNABLE_FIND_VALUE_FOR_KEY = "Unable to find value for key %s. Available keys %s";
    public static final String FILE_NOT_EXIST = "File %s does not exist in the [data] folder and its "
            + "subfolders by path %s";

    public static final String UNKNOWN_METHOD = "Unknown method %s";
    public static final String PAGES_FOLDER_NOT_EXIST = "[pages] folder does not exist";
    public static final String COMPONENTS_FOLDER_NOT_EXIST = "[components] folder does not exist";
    public static final String SCENARIOS_FOLDER_NOT_EXIST = "[scenarios] folder does not exist";
    public static final String FOLDER_LOCATION_ERROR_MESSAGE = "%s. Expected location -> %s";
    public static final String DATA_FOLDER_NOT_EXIST = "[data] folder does not exist";

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001b[0m";
    public static final String ANSI_RED_BOLD = "\033[1;31m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_CYAN = "\u001b[36m";

    public static final String DUPLICATE_FILENAME = ANSI_RED + "The [data] folder and its subfolders contain "
            + "files with duplicate filenames - %s. Every file should have a unique name" + ANSI_RESET;
    public static final String ERROR_LOG = "Error ->";
    public static final String POSITION_COMMAND_LOG = ANSI_YELLOW
            + "--------- Scenario step #%d - %s ---------" + ANSI_RESET;
    public static final String UI_COMMAND_LOG = ANSI_CYAN + "------- UI command #{} - {} -------" + ANSI_RESET;
    public static final String REPEAT_FINISHED_LOG = ANSI_CYAN + "------- Repeat is finished -------" + ANSI_RESET;
    public static final String COMMENT_LOG = format(TABLE_FORMAT, "Comment", "{}");
    public static final String ERROR_DURING_DB_MIGRATION_LOG = "Error during database migration ->";
    public static final String SCENARIO_NUMBER_AND_PATH_LOG = ANSI_GREEN
            + "================== Execute for scenario #{} - {} ==================" + ANSI_RESET;

    public static final String CANNOT_FIND_PROFILE = "Cannot find active profile option. "
            + "Please set profile to VM options.";
    public static final String EXECUTION_STOP_SIGNAL_LOG = "Execution has been stopped because of StopSignal";
    public static final String EXCEPTION_LOG = ANSI_RED
            + "----------------    EXCEPTION    -----------------\n"
            + " {}\n"
            + "--------------------------------------------------" + ANSI_RESET;
    public static final String AUTHENTICATE_WITH_CREDENTIALS_COMMENT = "Authenticate with credentials {}";
    public static final String FAILED_VARIABLE_WITH_PATH_LOG = "Failed [variable] {} [comment] {}";
    public static final String NO_ACTIVE_SCENARIOS_LOG = ANSI_RED + "There are no active scenarios by enabled tags"
            + ANSI_RESET;
    public static final String NO_ENABLE_TAGS_LOG = ANSI_RED + "There are no enable tags in runScriptByTag"
            + ANSI_RESET;

    public static final String NAME_LOG = format(TABLE_FORMAT, "Name", "{}");
    public static final String BY_URL_LOG = format(TABLE_FORMAT, "URL", "{}");
    public static final String RETHROWN_ERRORS_LOG = "Errors:%n %s";
    public static final String FAILED_VISITING_PATH_LOG = "Failed to visit path {}";
    public static final String REPEAT_COMMENT = "Repeat following steps";

    public static final String BROKER_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Action", "{}") + format("%n%19s| ", EMPTY) + format(TABLE_FORMAT, "Queue", "{}")
            + format("%n%19s| ", EMPTY) + format(TABLE_FORMAT, "Content", "{}");
    public static final String S3_ACTION_INFO_LOG = format(TABLE_FORMAT,
            "Action", "{}") + format("%n%19s| ", EMPTY) + format(TABLE_FORMAT,
            "Bucket", "{}") + format("%n%19s| ", EMPTY) + format(TABLE_FORMAT,
            "Key", "{}") + format("%n%19s| ", EMPTY) + format(TABLE_FORMAT,
            "File for action", "{}");

    public static final String SES_BODY_CONTENT_AND_TITLE_TEMPLATE = "%n%46s:%n%47s%-100s";
    public static final String CONFIG_FILE_NAME_INPUT_REQUIREMENTS =
            "Use one of the options: -c={configuration-file-name} or --config={configuration-file-name}\n"
            + "Please don't use spaces in the configuration file name. Allowed special character [._-:*#]\n"
            + "Please don't forget to include the file extension [.xml]";
    public static final String RESOURCES_PATH_INPUT_REQUIREMENTS =
            "Use one of the options: -p={path-to-your-test-resources) or --path={path-to-your-test-resources)";
    public static final String INVALID_ARGUMENTS_INPUT =
                      "\nIncorrect input of arguments. You must pass 2 arguments:"
                    + "\n\n1) Configuration file.\n" + CONFIG_FILE_NAME_INPUT_REQUIREMENTS
                    + "\n\n2) Path to test resources.\n" + RESOURCES_PATH_INPUT_REQUIREMENTS
                    + "\n\nExample: -c=config-file.xml -p=/user/folder/resources\n";
    public static final String INVALID_CONFIG_FILE_NAME_ARGUMENT =
                    "\n\nArgument of config file name incorrect, please follow the requirements below\n"
                    + CONFIG_FILE_NAME_INPUT_REQUIREMENTS
                    + "\nExample: -c=config-file.xml or --config=config-file.xml\n"
                    + "\nYou entered ---> %s\n";
    public static final String INVALID_PATH_TO_RESOURCES_ARGUMENT =
                    "\n\nArgument of path to resources folder incorrect, please follow the requirements below\n"
                    + RESOURCES_PATH_INPUT_REQUIREMENTS
                    + "\nExample: -p=/user/folder/resources or --path=/user/folder/resources\n"
                    + "\nYou entered ---> %s\n";
    public static final String TEST_EXECUTION_SUMMARY_TEMPLATE =
                    "\n\nTest run finished\n{} tests found\n{} tests skipped\n{} tests started\n{} test aborted\n"
                    + "{} test successful\n{} test failed\n";
    public static final String FAILED_SCENARIOS_NAME_TEMPLATE =
                   ANSI_RED_BOLD + "Scenario %s was failed. Related exception provided below." + ANSI_RED_BOLD;
    public static final String SUCCESS_QUERY = "Query completed successfully";
    public static final String TESTS_RUN_FAILED = "Test run failed";
    public static final String DROP_DOWN_OPERATION = "%s from drop down by %s, value for drop down method %s";
    public static final String JS_OPERATION_INFO = "File name [%s]%n Commands: %n%s";
    public static final String NAVIGATE_NOT_SUPPORTED = "Navigate command %s not supported";
    public static final String DROP_DOWN_NOT_SUPPORTED = "Drop down by method by %s not supported";
    public static final String SCROLL_TO_ELEMENT_NOT_SUPPORTED = "Scroll to the element by %s percents not supported";
    public static final String JS_EXECUTION_OPERATION = "JavaScript execution operation";
    public static final String JS_FILE_UNREADABLE = "The .js file by path ./javascript/%s unreadable";
    public static final String JS_FILE_NOT_FOUND = "Can't find the .js file [%s] in the resources folder";
    public static final String DROP_DOWN_ONE_VALUE = "Process drop down one value";

    public static final String PATCH_PATH_LOG = format(TABLE_FORMAT, "Migration patch ", "{}");
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
    public static final String WAIT_COMMAND = "Wait command";
    public static final String SECOND_TAB_NOT_FOUND = "Second tab not found";
    public static final String CLOSE_TAB_ACTION = "Close action";
    public static final String CLEAR_ACTION = "Clear action";
    public static final String CLEAR_ACTION_LOCATOR = "Element locator for clear %s";
    public static final String CLOSE_TAB_INFO = "Close second tab";
    public static final String DRIVER_INITIALIZER_NOT_FOUND = "Driver initializer not found";
    public static final String BODY_LOG = format(TABLE_FORMAT, "Body", "{}");
    public static final String UI_EXECUTION_TIME_LOG = format(TABLE_FORMAT, "Step execution time", "{} ms");
    public static final String ENDPOINT_LOG = format(TABLE_FORMAT, "Endpoint", "{}");
    public static final String UI_COMMAND_EXEC_TIME = "Ui command execution time";
    public static final String COMMAND_TYPE_LOG = format(TABLE_FORMAT, "Command type", "{}");
    public static final String SHELL_FILE_LOG = format(TABLE_FORMAT, "Shell file", "{}");
    public static final String SHELL_COMMAND_LOG = format(TABLE_FORMAT, "Shell command", "{}");
    public static final String SCROLL_DIRECTION_LOG = format(TABLE_FORMAT, "Direction", "{}");
    public static final String SCROLL_BY_LOG = format(TABLE_FORMAT, "Scroll by", "{}");
    public static final String LINE =
            "-----------------------------------------------------------------------------------------------------"
                    + "--------------------------------------------------------------------------------------";
    public static final String INVALID_SCENARIO_LOG = ANSI_RED + LINE
            + "\n" + format("%21s", EMPTY) + format(TABLE_FORMAT, "Invalid scenario", "{}") + "\n"
            + format("%21s", EMPTY) + format(TABLE_FORMAT, "Exception", "{}") + "\n" + format("%21s", EMPTY)
            + LINE + ANSI_RESET + "\n";
    public static final String VALID_SCENARIOS_NOT_FOUND = ANSI_RED + "Valid scenarios are not found" + ANSI_RESET;
    public static final String SCROLL_ACTION = "Scroll action";
    public static final String SCROLL_TO_INFO = "Scroll to element with locator %s";
    public static final String SCROLL_INFO = "Scroll %s, by %s, by value %s";
    public static final String NOT_ENABLED_BROWSERS = "At least 1 browser must be enabled";
    public static final String UI_DISABLED_ERROR = "Your scenarios contain UI testing steps, but your UI settings is "
            + "disabled. Please enable UI settings in config file or exclude scenarios with UI steps.";

    public static final String TIMES_LOG = format(TABLE_FORMAT, "Times to repeat", "{}");
    public static final String REPEAT_COMMAND = "Repeat command";
    public static final String REPEAT_INFO = "Repeat UI commands %d times";

    public static final String CREDENTIALS_LOG = format(TABLE_FORMAT, "Credentials", "{}");
    public static final String AUTH_WAS_NOT_DEFINED = "Authentication strategy has not been defined in the "
            + "configuration file. Usage example: <auth strategy=“basic”.../>";

    public static final String LOCAL_STORAGE_KEY = format(TABLE_FORMAT, "Local storage key", "{}");
    public static final String CLEAR_COOKIES_AFTER = format(TABLE_FORMAT, "Clear cookies after", "{}");
}
