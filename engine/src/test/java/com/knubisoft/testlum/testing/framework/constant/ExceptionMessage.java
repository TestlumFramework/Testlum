package com.knubisoft.testlum.testing.framework.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings.INTEGRATION_CONFIG_FILENAME;
import static com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings.UI_CONFIG_FILENAME;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ANSI_RED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ANSI_RESET;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.ANSI_YELLOW;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ExceptionMessage {
    // general
    public static final String NOT_DECLARED_WITH_INTERPRETER_FOR_CLASS = "Each non abstract interpreter must "
                                                                         + "declare annotation InterpreterForClass. Class %s do not have this annotation";
    public static final String FUNCTION_FOR_COMMAND_NOT_FOUND = "Function for class %s not found. "
                                                                + "Please register algorithm for interpreter";
    public static final String NOT_DECLARED_WITH_EXECUTOR_FOR_CLASS = "Each non abstract executor must "
                                                                      + "declare annotation ExecutorForClass. Class %s do not have this annotation";
    public static final String EXECUTOR_FOR_UI_COMMAND_NOT_FOUND = "Executor for class %s not found";
    public static final String INTERPRETER_NOT_FOUND = "Interpreter for class %s not found";
    public static final String MISSING_CONSTRUCTOR = "Missing constructor for class: %s";

    public static final String ALIAS_BY_STORAGE_NAME_NOT_FOUND = "Alias for data storage [%s] not found. "
                                                                 + "Available aliases are %s";

    public static final String XSDISSUE_TO_STRING =
            "XSDIssue{message='%s', lineNumber=%d, columnNumber=%d, path=%s}";
    public static final String RETHROWN_ERRORS_TEMPLATE = "Errors:%n%s";
    public static final String UNABLE_FIND_FILE_IN_ROOT_FOLDER = "Unable to find file by key [%1$s] "
                                                                 + "Initial scan folder [%2$s] with strategy recursive walk to root folder [%3$s]";
    public static final String FOLDER_LOCATION_ERROR_MESSAGE = "%s. Expected location -> %s";
    public static final String SCENARIOS_FOLDER_NOT_EXIST = "[scenarios] folder does not exist";
    public static final String PAGES_FOLDER_NOT_EXIST = "[pages] folder does not exist";
    public static final String COMPONENTS_FOLDER_NOT_EXIST = "[components] folder does not exist";
    public static final String DATA_FOLDER_NOT_EXIST = "[data] folder does not exist";
    public static final String ENV_CONFIG_FOLDER_NOT_EXIST = "[config] folder does not exist";
    public static final String ENV_FOLDER_NOT_EXIST = "[%s] folder does not exist";
    public static final String DUPLICATE_FILENAME = ANSI_RED + "The [%s] folder and its subfolders contain "
                                                    + "files with duplicate filenames - [%s]. Each file should have a unique name" + ANSI_RESET;
    public static final String DUPLICATE_FOLDER_NAME = ANSI_RED + "The [%s] folder and its subfolders contain "
                                                       + "folders with duplicate names - [%s]. Each folder should have a unique name" + ANSI_RESET;

    public static final String NO_ENABLED_ENVIRONMENTS_FOUND = "No enabled environments found in configuration file";
    public static final String NO_ENABLED_REPORT_GENERATORS_FOUND = ANSI_RED +
                                                                    "At least one report generator must be enabled" + ANSI_RESET;
    public static final String STOP_IF_NON_PARSED_SCENARIO = ANSI_YELLOW
                                                             + "The execution has been stopped because of the enabled <StopIfInvalidScenario>" + ANSI_RESET;
    public static final String VALID_SCENARIOS_NOT_FOUND = ANSI_RED + "Valid scenarios are not found" + ANSI_RESET;
    public static final String NO_SCENARIOS_FILTERED_BY_TAGS = ANSI_RED
                                                               + "There are no active scenarios by enabled tags" + ANSI_RESET;
    public static final String NO_ENABLED_TAGS_CONFIG = ANSI_RED
                                                        + "No enabled tags in runScenariosByTag configuration" + ANSI_RESET;

    // scenario validation
    public static final String INTEGRATION_NOT_FOUND = "Cannot find integration configuration for <%s>";
    public static final String ALIAS_NOT_FOUND = "Cannot find enabled integration with alias <%s>";
    public static final String API_NOT_FOUND = "Cannot find api with alias <%s>";
    public static final String AUTH_NOT_FOUND = "Cannot find <auth> configuration for <api> with alias <%s>";
    public static final String AUTH_ALIASES_DOESNT_MATCH = "<Http> command alias doesn't match with <auth> alias";

    public static final String SCENARIO_CANNOT_BE_INCLUDED_TO_ITSELF = "Scenario cannot be included to itself";
    public static final String UNSUPPORTED_MIGRATION_FORMAT = "Unsupported data file format was found "
                                                              + "in <migration> tag. For %s allowed extensions are: %s, %s";
    public static final String DB_NOT_SUPPORTED = "Database by name %s not supported";

    public static final String SAME_APPIUM_URL = "Can`t use the same Appium server URL for scenario with "
                                                 + "<mobilebrowser> & <native> tags";
    public static final String SAME_MOBILE_DEVICES = "Can`t use the same mobile devices for scenario with "
                                                     + "<mobilebrowser> & <native> tags";
    public static final String CONNECTION_TYPE_NOT_DEFINED = ANSI_RED
                                                             + "Connection type not defined in the <%s> config by path: %s" + ANSI_RESET;
    public static final String DEVICE_CONNECTION_CAPABILITIES_NOT_DEFINED = ANSI_RED
                                                                            + "Appropriate connection capabilities for device with alias <%s> not defined "
                                                                            + "in the <%s> config by path: %s" + ANSI_RESET;
    public static final String NOT_ENABLED_BROWSERS = "Web configuration should be declared "
                                                      + "and enabled. At least 1 browser must be enabled";
    public static final String NOT_ENABLED_MOBILEBROWSER_DEVICE = "Mobilebrowser configuration should be declared "
                                                                  + "and enabled. At least 1 mobilebrowser device must be enabled";
    public static final String NOT_ENABLED_NATIVE_DEVICE = "Native configuration should be declared "
                                                           + "and enabled. At least 1 native device must be enabled";
    public static final String NO_LOCATOR_FOUND_FOR_INNER_SCROLL = "No locator found for the inner scroll";
    public static final String NO_LOCATOR_FOUND_FOR_ELEMENT_SWIPE = "No locator found for the element swipe";

    public static final String DRIVER_INITIALIZER_NOT_FOUND = "Driver initializer not found";
    public static final String UNKNOWN_MOBILE_PLATFORM_NAME = "Unknown mobile platform name: %s";
    public static final String UNKNOWN_CONNECTION_TYPE = "Unknown connection type: %s";
    public static final String BROWSER_STACK_CONFIGURATION_NOT_FOUND = "Cannot find <BrowserStackLogin> configuration";

    public static final String WEB_DRIVER_NOT_INIT = "The webDriver for <web> has not been initialized, "
                                                     + "check your configuration in config file";
    public static final String MOBILEBROWSER_DRIVER_NOT_INIT = "The webDriver for <mobilebrowser> has not been "
                                                               + "initialized, check your configuration in config file";
    public static final String NATIVE_DRIVER_NOT_INIT = "The webDriver for <native> has not been initialized, "
                                                        + "check your configuration in config file";

    // config validation
    public static final String UI_CONFIG_NOT_PRESENT_IN_ALL_ENVS = ANSI_RED + "Number of enabled <%s> blocks does not"
                                                                   + " match the number of enabled environments" + ANSI_RESET;
    public static final String BASE_URLS_ARE_SAME = ANSI_RED + "<baseUrl> must be different for each <%s> block in all "
                                                    + "enabled environments (" + UI_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String ENVIRONMENT_MISSING_DEVICES_OR_BROWSERS = ANSI_RED + "One or more of your environments "
                                                                         + "does not have enabled <%ss> in <%s> block" + ANSI_RESET;
    public static final String UI_ALIASES_NOT_DIFFER = ANSI_RED + "More than one enabled <%s> was found in "
                                                       + "<%s> settings with alias <%s> in the config by path: %s" + ANSI_RESET;
    public static final String UI_ALIASES_NOT_MATCH = ANSI_RED + "Enabled %s <%s> in <%s> block with alias <%s> not"
                                                      + " found or not enabled in all enabled environments (" + UI_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String CONNECTION_TYPE_NOT_MATCH = ANSI_RED + "Connection type in <%s> block must be the same "
                                                           + "in all enabled environments (" + UI_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String SAME_APPIUM_SERVER_URLS = ANSI_RED + "<AppiumServer> connection url must be different "
                                                         + "for each <%s> settings in all enabled environments (" + UI_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String BROWSERSTACK_LOGIN_NOT_CONFIGURED = ANSI_RED + "To use the <BrowserStack> connection "
                                                                   + "type requires <browserStackLogin> settings to be configured in all enabled environments "
                                                                   + "(" + UI_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String DEVICE_PLATFORMS_NOT_MATCH = ANSI_RED + "<device> in <%s> block with alias <%s> must "
                                                            + "have the same <platformName> param in all enabled environments (" + UI_CONFIG_FILENAME + " files)"
                                                            + ANSI_RESET;
    public static final String CAPABILITIES_TYPE_NOT_MATCH_WITH_CONNECTION_TYPE = ANSI_RED + "<device> with alias <%s> "
                                                                                  + "in <%s> block has capabilities type that does not match with block <connection> type in the config by "
                                                                                  + "path: %s" + ANSI_RESET;
    public static final String CAPABILITIES_TYPE_NOT_MATCH_IN_ALL_ENVS = ANSI_RED + "<device> with alias <%s> in <%s> "
                                                                         + "block has capabilities type that does not match with this device capabilities type in other enabled "
                                                                         + "environments (" + UI_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String INTEGRATIONS_MISMATCH_ENVS = ANSI_RED + "<%s> integration must be configured and "
                                                            + "enabled in all enabled environments or not configured/disabled at all" + ANSI_RESET;
    public static final String SAME_INTEGRATION_ALIAS = ANSI_RED + "More than one enabled <%s> integration was found "
                                                        + "with alias <%s> in the config by path: %s" + ANSI_RESET;
    public static final String INTEGRATION_ALIAS_NOT_MATCH = ANSI_RED + "<%s> integration with alias <%s> not found "
                                                             + "in all enabled environments (" + INTEGRATION_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String AUTH_NOT_PRESENT_IN_ALL_ENVS = ANSI_RED + "<Api> integration with alias <%s> have "
                                                              + "configuration for <auth> command, but not in all enabled environments (" + INTEGRATION_CONFIG_FILENAME
                                                              + " files)" + ANSI_RESET;
    public static final String AUTH_LOGOUT_NOT_MATCH = ANSI_RED + "<Api> integration with alias <%s> must have the "
                                                       + "same setup for <autoLogout> argument in <auth> command in all enabled environments ("
                                                       + INTEGRATION_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String AUTH_STRATEGY_NOT_MATCH = ANSI_RED + "<Api> integration with alias <%s> must have the "
                                                         + "same setup for <authStrategy> argument in <auth> command in all enabled environments ("
                                                         + INTEGRATION_CONFIG_FILENAME + " files)" + ANSI_RESET;
    public static final String AUTH_CUSTOM_CLASS_NAME_NOT_MATCH = ANSI_RED + "<Api> integration with alias <%s> must "
                                                                  + "have the same setup for <authCustomClassName> argument in <auth> command in all enabled environments"
                                                                  + " (" + INTEGRATION_CONFIG_FILENAME + " files)" + ANSI_RESET;

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
            + "\nExample for MacOs and Linux: -p=/user/folder/resources or --path=/user/folder/resources"
            + "\nExample for Windows: -p=\\user\\folder\\resources or --path=\\user\\folder\\resources\n"
            + "\nYou entered ---> %s\n";
    public static final String INVALID_PATH_TO_INITIAL_STRUCTURE_GENERATION_ARGUMENT =
            "\n\nArgument of path to folder for initial structure incorrect, please follow the requirements below\n"
            + "Use one of the options: -g={path-to-initial-structure) or --generate={path-to-initial-structure)"
            + "\nExample: -g=/user/folder/resources or --generate=/user/folder/resources\n"
            + "\nYou entered ---> %s\n";
    public static final String FAILED_CONDITION_EXPRESSION = ANSI_RED
                                                             + "Condition '%s' is not a valid boolean expression\n"
                                                             + "Converted condition: %s\n"
                                                             + "Parsing exception: %s" + ANSI_RESET;

    public static final String VARIATIONS_NOT_FOUND = "Variations for %s not found";
    public static final String VARIATION_FILE_IS_EMPTY = "Variation %s file in the %s is empty";
    public static final String VARIATIONS_NOT_USED = "Variations are present in the %s but not used";
    public static final String ENV_VARIABLE_NOT_FOUND = "No such environmental variable as <%s>";

    public static final String UNABLE_PARSE_FILE_WITH_LOCATORS = "Unable to parse file %s with locators. Error: %s";
    public static final String LOCATOR_ID_HAS_DUPLICATE_LOCATOR =
            "Locator id from page <%s> has duplicate locator with id <%s>";
    public static final String INCORRECT_NAMING_FOR_LOCATOR_ID =
            "Incorrect naming for %s. Expect format: <pageName.locatorId>";
    public static final String UNABLE_TO_FIND_LOCATOR_BY_PATH = "Unable to find locator [%s] by path";
    public static final String WEB_ELEMENT_BY_LOCATOR_NOT_FOUND = "Web element for locator with id <%s> not found";
    public static final String FOUND_MORE_THEN_ONE_ELEMENT = "More than one element was found by locator <%s>";

    // interpreters
    public static final String SLOW_COMMAND_PROCESSING = "Slow command processing detected. "
                                                         + "Took %d ms, threshold %d ms";
    public static final String HTTP_CODE_EXPECTED_BUT_WAS = " Http code should be [%s] but was [%s]";
    public static final String HTTP_HEADERS_EXPECTED_BUT_WAS = " Http headers should be [%s]%n but was [%s]";
    public static final String HTTP_BODY_EXPECTED_BUT_WAS = " Http body should be [%s]%n but was [%s]";
    public static final String UNKNOWN_HTTP_METHOD = "Unknown http method: %s";
    public static final String UNKNOWN_BODY_CONTENT = "Unknown body content provided";
    public static final String INCORRECT_HTTP_PROCESSING = "Incorrect http processing";
    public static final String REDIS_COMMAND_NOT_FOUND = "Redis command was not provided";
    public static final String TIME_UNIT_UNKNOWN_TYPE = "Time unit unknown type: %s";
    public static final String FAILED_CONNECTION_TO_DATABASE =
            "Connection to database with name \"%s\" failed. "
            + "Please check global configuration, it may be disabled or doesn't exist";
    public static final String UNEXPECTED_WEBSOCKET_MESSAGE_TYPE = "Unexpected websocket message type: %s";

    // executors
    public static final String VAR_TYPE_NOT_SUPPORTED = "Type of <%s> command is not supported";
    public static final String VAR_QUERY_RESULT_ERROR =
            "Query result is empty. Perhaps your database is empty or there is no such value in the database";
    public static final String GENERATION_METHOD_NOT_SUPPORTED = "Random string generation method is not supported";
    public static final String ASSERT_TYPE_NOT_SUPPORTED = "Assert command %s not supported";
    public static final String HOT_KEY_NOT_SUPPORTED = "Hotkey command %s not supported";
    public static final String NAVIGATE_NOT_SUPPORTED = "Navigate command %s not supported";
    public static final String DROP_DOWN_NOT_SUPPORTED = "Drop down by method by %s not supported";
    public static final String CUSTOM_DROP_DOWN_NOT_SUPPORTED =
            "Provided drop down by method %s not supported.\n"
            + "For custom drop down You can select only one value by text per scenario step";
    public static final String JS_FILE_UNREADABLE = "The .js file by path ./javascript/%s unreadable";
    public static final String IMAGES_SIZE_MISMATCH = "The images don't match. Image comparison state is -> "
                                                      + "SIZE_MISMATCH%n" + "Expected image size: %sx%s%n" + "Actual image size: %sx%s";
    public static final String IMAGES_MISMATCH = "The images don't match. Image comparison state is -> MISMATCH. "
                                                 + "Difference percent: %s";
    public static final String IOS_NOT_SUPPORT_PART_COMMAND = "iOS mobilebrowser does not support <part> command";
    public static final String WEB_ELEMENT_ATTRIBUTE_NOT_EXIST = "Web element does not have attribute <%s>";
    public static final String ELEMENT_NOT_FOUND = "Element with locator <%s> was not found during 'scrollTo'";
    public static final String DRAG_AND_DROP_FILE_NOT_FOUND = "Drag and drop file with name <%s> not found";
    public static final String SCROLL_TO_ELEMENT_NOT_SUPPORTED = "Scroll to the element by %s percents not supported";
    public static final String SCROLL_TYPE_NOT_FOUND = "Scroll type <%s> not supported";
    public static final String INVALID_LOCATOR =
            "Locator type is not supported for INNER scroll. Your locator -> <%s>.";
    public static final String SWIPE_TYPE_NOT_FOUND = "Swipe direction <%s> is not supported";
    public static final String TAB_NOT_FOUND = "No tab found to work with";
    public static final String TAB_OUT_OF_BOUNDS = "Cannot close the tab №<%s> because there are only %s tabs";
    public static final String CANNOT_SWITCH_TO_WEBVIEW = "Cannot switch to the web view. Web context was not found";
}
