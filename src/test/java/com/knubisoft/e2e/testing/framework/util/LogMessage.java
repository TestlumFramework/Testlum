package com.knubisoft.e2e.testing.framework.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.EMPTY;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LogMessage {

    public static final String HTTP_CODE_EXPECTED_BUT_WAS = " Http code should be [%s] but was [%s]";
    public static final String HTTP_HEADERS_EXPECTED_BUT_WAS = " Http headers should be [%s] but was [%s]";
    public static final String HTTP_BODY_EXPECTED_BUT_WAS = " Http body should be [%s] but was [%s]";
    public static final String INCORRECT_HTTP_PROCESSING = "Incorrect http processing";
    public static final String INCORRECT_S3_PROCESSING = "Incorrect S3 processing";
    public static final String INCORRECT_SQS_PROCESSING = "Incorrect SQS processing";
    public static final String SES_DESTINATION_AND_SOURCE_LOG = format("Destination -> {}.%n%-20s Source -> {}", EMPTY);
    public static final String ELASTICSEARCH_METHOD_AND_URL_LOG = "Elasticsearch {} request. URL - {}";
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

    public static final String FILE_NOT_FOUND_WITH_CREDS = "File with credentials for authentication "
            + "not found by path: %s";

    public static final String UNABLE_AUTHENTICATE_WITH_CREDS = "Unable to authenticate with credentials%n"
            + "Email:%s%n"
            + "Password:%s%n"
            + "=========================%n"
            + "Exception%n%s%n";

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
    public static final String FILE_NOT_EXIST = "File %s does not exist";
    public static final String UNKNOWN_METHOD = "Unknown method %s";
    public static final String PAGES_FOLDER_NOT_EXIST = "Pages folder does not exist";
    public static final String COMPONENTS_FOLDER_NOT_EXIST = "Components folder does not exist";
    public static final String SCENARIOS_FOLDER_NOT_EXIST = "Scenarios folder does not exist";
    public static final String VARIATIONS_FOLDER_NOT_EXIST = "Variations folder does not exist";
    public static final String PATCHES_FOLDER_NOT_EXIST = "Patches folder does not exist";
    public static final String CREDENTIALS_FOLDER_NOT_EXIST = "Credentials folder does not exist";
    public static final String FOLDER_LOCATION_ERROR_MESSAGE = "%s. Expected location -> %s";
    public static final String TEST_BY_PATH_DISABLED = "Test by path %s is disabled";

    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_RESET = "\u001b[0m";
    public static final String ANSI_RED_BOLD = "\033[1;31m";

    public static final String ERROR_LOG = "Error ->";
    public static final String OVERVIEW_LOG = "Overview:";
    public static final String POSITION_COMMAND_LOG = "%2s. %-8s";
    public static final String COMMENT_LOG = "Comment: {}";
    public static final String ERROR_DURING_DB_MIGRATION_LOG = "Error during database migration ->";
    public static final String EXECUTE_SCENARIO_LOG = ANSI_GREEN + "Execute for scenario" + ANSI_RESET + " {}";

    public static final String CANNOT_FIND_PROFILE = "Cannot find active profile option. "
            + "Please set profile to VM options.";
    public static final String EXECUTION_STOP_SIGNAL_LOG = "Execution has been stopped because of StopSignal";
    public static final String EXCEPTION_LOG = ANSI_RED + "Exception ->" + ANSI_RESET + " {}";
    public static final String AUTHENTICATE_WITH_CREDENTIALS_COMMENT = "Authenticate with credentials %s";
    public static final String FAILED_VARIABLE_WITH_PATH_LOG = "Failed [variable] {} [comment] {}";
    public static final String TEMPLATE_LOG = "[Value in contextOf] = {}";
    public static final String BY_URL_LOG = "By URL [{}]";
    public static final String RETHROWN_ERRORS_LOG = "Errors:%n %s";
    public static final String FAILED_VISITING_PATH_LOG = "Failed to visit path {}";
    public static final String REPEAT_COMMENT = "Repeat following steps";
    public static final String QUERY_LOG_TEMPLATE = "%10s %-100s";
    public static final String BROKER_ACTION_INFO_LOG = format(
            "Action - {}.%n%-20s Queue - {}.%n%-20s Content - {}", EMPTY, EMPTY);
    public static final String S3_ACTION_INFO_LOG = format(
            "Action - {}.%n%-20s Bucket - {}.%n%-20s Key - {}.%n%-20s File for action - {}", EMPTY, EMPTY, EMPTY);
    public static final String SES_BODY_CONTENT_AND_TITLE_TEMPLATE = "%n%30s:%n%31s%-100s";
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
}
