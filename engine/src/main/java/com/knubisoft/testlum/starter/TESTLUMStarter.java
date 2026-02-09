package com.knubisoft.testlum.starter;


import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.boot.Banner;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.PropertySource;

import java.io.File;
import java.util.*;
import java.util.function.Supplier;

/**
 * Main entry point for launching Testlum test execution.
 *
 * <p>This class provides the primary mechanism for running Testlum tests from the command line
 * or programmatically. It handles configuration initialization, test discovery, execution,
 * and result reporting.</p>
 *
 * <h2>Usage</h2>
 *
 * <h3>Command Line</h3>
 * <pre>{@code
 * java -jar testlum.jar -c=config.xml -p=/path/to/resources
 * }</pre>
 *
 * <h3>Programmatic</h3>
 * <pre>{@code
 * public static void main(String[] args) {
 *     TESTLUMStarter.main(new String[]{"-c=global-config.xml", "-p=/path/to/resources"});
 * }
 * }</pre>
 *
 * <h2>Command Line Arguments</h2>
 * <ul>
 *   <li>{@code -c=<file>} or {@code --config=<file>} - Configuration file name (required)</li>
 *   <li>{@code -p=<path>} or {@code --path=<path>} - Path to test resources directory (required)</li>
 *   <li>{@code -s=<path>} or {@code --scenario=<path>} - Path to specific scenarios (optional)</li>
 * </ul>
 *
 * <h2>Exit Codes</h2>
 * <ul>
 *   <li>{@code 0} - All tests passed successfully</li>
 *   <li>{@code 1} - One or more tests failed</li>
 *   <li>{@code 2} - No tests were found</li>
 *   <li>{@code 3} - Invalid configuration (missing arguments or files not found)</li>
 * </ul>
 *
 * @see TestResourceSettings
 * @see com.knubisoft.testlum.testing.RootTest
 * @see ExitCode
 */

@SpringBootApplication
@Slf4j
@PropertySource(name = "spring.banner.location", value = "classpath:banner.txt")
@PropertySource(name = "logging.level.root", value = "INFO")
public class TESTLUMStarter {

    /**
     * Main entry point for Testlum test execution.
     *
     * <p>This method performs the following steps:</p>
     * <ol>
     *   <li>Parses command line arguments for configuration file, resource path, and optional scenario scope</li>
     *   <li>Initializes test resource settings with the provided configuration</li>
     *   <li>Sets up UI locators folder if any UI configuration is enabled</li>
     *   <li>Launches JUnit Platform to discover and execute tests</li>
     *   <li>Reports execution summary and exits with appropriate exit code</li>
     * </ol>
     *
     * @param args command line arguments:
     *             <ul>
     *               <li>{@code -c=<file>} or {@code --config=<file>} - configuration file (required)</li>
     *               <li>{@code -p=<path>} or {@code --path=<path>} - test resources path (required)</li>
     *               <li>{@code -s=<path>} or {@code --scenario=<path>} - specific scenarios path (optional)</li>
     *             </ul>
     * @throws IllegalArgumentException if required arguments (config file or path) are not provided
     */
    public static void main(final String[] args) {
        Optional<String> configFileName = Args.read(args, Args.Param.CONFIG_FILE);
        Optional<String> pathToTestResources = Args.read(args, Args.Param.PATH_TO_TEST_RESOURCES);
        Optional<String> scenarioScope = Args.read(args, Args.Param.PATH_TO_SPECIFIC_SCENARIOS);

        validateParams(configFileName, pathToTestResources);

        TestResourceSettings.init(configFileName.get(), pathToTestResources.get(), scenarioScope);
        System.setProperty("resource", pathToTestResources.get());
        initLocatorsFolder();

        new SpringApplicationBuilder(TestRunner.class)
                .bannerMode(Banner.Mode.CONSOLE)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    /**
     * Validates command line parameters for configuration and path.
     *
     * <p>Performs two-phase validation: first checks if required parameters are present,
     * then verifies that the specified files and directories exist.</p>
     *
     * @param config optional configuration file name
     * @param path   optional path to test resources directory
     */
    private static void validateParams(final Optional<String> config, final Optional<String> path) {
        validateRequiredParams(config, path);
        validateFilesExist(config.get(), path.get());
    }

    /**
     * Validates that required parameters are present.
     *
     * @param config  optional configuration file name
     * @param path    optional path to test resources
     */
    private static void validateRequiredParams(final Optional<String> config,
                                               final Optional<String> path) {
        InputValidator.construct()
                .check(Args.Param.CONFIG_FILE.name(),
                        config::isEmpty, "Required parameter is missing")
                .check(Args.Param.PATH_TO_TEST_RESOURCES.name(),
                        path::isEmpty, "Required parameter is missing")
                .exitIfAny();
    }

    /**
     * Validates that the resources directory and configuration file exist.
     *
     * @param config  configuration file name
     * @param path    path to test resources directory
     */
    private static void validateFilesExist(final String config,
                                           final String path) {
        File resourcesDir = new File(path);
        InputValidator.construct()
                .check(Args.Param.PATH_TO_TEST_RESOURCES.name(),
                        () -> !resourcesDir.exists(), "Folder does not exist")
                .check(Args.Param.CONFIG_FILE.name(),
                        () -> !new File(resourcesDir, config).exists(), "File not found")
                .exitIfAny();
    }

    /**
     * Fluent input validator for checking command line parameters.
     *
     * <p>Collects validation errors and provides usage information when errors are found.</p>
     */
    private static class InputValidator {

        private final Map<String, String> errors = new LinkedHashMap<>(3, 1F);

        /**
         * Creates a new InputValidator instance.
         *
         * @return new validator instance
         */
        public static InputValidator construct() {
            return new InputValidator();
        }

        /**
         * Adds a validation check for a parameter.
         *
         * @param key     parameter name for error reporting
         * @param check   supplier that returns true if validation fails
         * @param message error message to display on failure
         * @return this validator for method chaining
         */
        public InputValidator check(final String key, final Supplier<Boolean> check, final String message) {
            try {
                boolean error = check.get();
                if (error) {
                    errors.put(key, message);
                }
            } catch (Exception e) {
                errors.put(key, message);
            }
            return this;
        }

        /**
         * Executes callback if any validation errors were collected.
         *
         * <p>Prints errors and usage information if has any errors.</p>
         */
        public void exitIfAny() {
            if (!errors.isEmpty()) {
                printErrorsAndUsage();
                System.exit(ExitCode.INVALID_CONFIGURATION.exitCode);
            }
        }

        /**
         * Logs validation errors and prints usage information.
         */
        private void printErrorsAndUsage() {
            log.error("Failed to start Testlum due to configuration errors:");
            errors.forEach((param, message) -> log.error("  - {}: {}", param, message));
            log.info("");
            printUsage();
        }

        /**
         * Prints command line usage information to the log.
         */
        private void printUsage() {
            String usage = """
                Usage: java -jar testlum.jar [options]

                Required arguments:
                  -c=<file>, --config=<file>    Configuration file name (e.g., global-config.xml)
                  -p=<path>, --path=<path>      Absolute path to test resources directory

                Optional arguments:
                  -s=<path>, --scenario=<path>  Path to specific scenarios to execute

                Example:
                  java -jar testlum.jar -c=config.xml -p=/home/user/test-resources

                Exit codes: 0=passed, 1=failed, 2=no tests, 3=invalid config""";
            log.info(usage);
        }
    }

    /**
     * Exit codes returned by the Testlum test execution.
     *
     * <p>These codes follow JUnit Console Launcher conventions:</p>
     * <ul>
     *   <li>{@link #TESTS_PASSED} (0) - All tests executed successfully</li>
     *   <li>{@link #TESTS_FAILED} (1) - One or more tests failed or encountered errors</li>
     *   <li>{@link #NO_TESTS_FOUND} (2) - No tests were discovered for execution</li>
     *   <li>{@link #INVALID_CONFIGURATION} (3) - Configuration is invalid or files not found</li>
     * </ul>
     */
    @Getter
    @RequiredArgsConstructor
    enum ExitCode {

        /** All tests passed successfully. Exit code: 0 */
        TESTS_PASSED(0, "Tests passed"),

        /** One or more tests failed. Exit code: 1 */
        TESTS_FAILED(1, "Tests failed"),

        /** No tests were found for execution. Exit code: 2 */
        NO_TESTS_FOUND(2, "No tests found"),

        /** Invalid configuration. Exit code: 3 */
        INVALID_CONFIGURATION(3, "Invalid configuration");

        private final int exitCode;
        private final String message;
    }

    /**
     * Determines the appropriate exit code based on test execution summary.
     *
     * @param summary the test execution summary
     * @return the exit code corresponding to the test results
     */
    static @NotNull ExitCode getExitCode(final TestExecutionSummary summary) {
        final ExitCode code;
        if (summary.getTestsFoundCount() == 0) {
            code = ExitCode.NO_TESTS_FOUND;
        } else if (summary.getTestsFailedCount() > 0) {
            code = ExitCode.TESTS_FAILED;
        } else {
            code = ExitCode.TESTS_PASSED;
        }
        return code;
    }

    /**
     * Initializes the locators folder if any UI configuration is enabled.
     *
     * <p>Checks all UI configurations (web, native, mobile browser) and initializes
     * the locators folder only when at least one UI testing mode is enabled.</p>
     */
    private static void initLocatorsFolder() {
        Map<String, UiConfig> uiConfigs = GlobalTestConfigurationProvider.get().getUiConfigs();

        boolean isUiConfigEnabled = uiConfigs.values().
                stream().anyMatch(TESTLUMStarter::isUiConfigEnabled);

        if (isUiConfigEnabled) {
            TestResourceSettings.getInstance().initLocatorsFolder();
        }
    }

    /**
     * Checks if any UI testing mode is enabled in the given configuration.
     *
     * @param uiConfig the UI configuration to check
     * @return {@code true} if web, native, or mobile browser testing is enabled
     */
    private static boolean isUiConfigEnabled(final UiConfig uiConfig) {
        boolean webEnabled = Optional.ofNullable(uiConfig.getWeb()).
                map(Web::isEnabled).orElse(false);
        boolean nativeEnabled = Optional.ofNullable(uiConfig.getNative()).
                map(Native::isEnabled).orElse(false);
        boolean mobileEnabled = Optional.ofNullable(uiConfig.getMobilebrowser()).
                map(Mobilebrowser::isEnabled).orElse(false);
        return webEnabled || nativeEnabled || mobileEnabled;
    }
}
