package com.knubisoft.testlum.starter;

import com.knubisoft.testlum.testing.RootTest;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.env.parallel.GlobalParallelExecutionConfigStrategy;
import com.knubisoft.testlum.testing.framework.util.Args;
import com.knubisoft.testlum.testing.model.global_config.Mobilebrowser;
import com.knubisoft.testlum.testing.model.global_config.Native;
import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import com.knubisoft.testlum.testing.model.global_config.Web;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

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
 * </ul>
 *
 * @see TestResourceSettings
 * @see RootTest
 */
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

        TestResourceSettings.init(configFileName.orElseThrow(() ->
                        new IllegalArgumentException("No config file provided as first argument")),
                pathToTestResources.orElseThrow(() ->
                        new IllegalArgumentException("No path to test resources provided as second argument")),
                scenarioScope);

        System.setProperty("resource", pathToTestResources.get());

        initLocatorsFolder();

        TestExecutionSummary summary = launchTestsAndGetSummary();
        formatMessageAndExitCode(summary);
    }

    /**
     * Formats the test execution summary and exits with the appropriate exit code.
     *
     * <p>Logs the execution status and detailed summary at INFO level,
     * and any failures at ERROR level before terminating the JVM.</p>
     *
     * @param summary the test execution summary containing results
     */
    private static void formatMessageAndExitCode(final TestExecutionSummary summary) {
        ExitCode exitCode = getExitCode(summary);

        Logger log4jLogger = LogManager.getLogger(TESTLUMStarter.class);

        log4jLogger.info("Execution status [{}]", exitCode.getMessage());
        with(log4jLogger, Level.INFO, summary::printTo);
        with(log4jLogger, Level.ERROR, summary::printFailuresTo);

        System.exit(exitCode.getExitCode());
    }

    /**
     * Utility method to write output to a logger via PrintWriter.
     *
     * @param log4jLogger the logger to write to
     * @param level       the log level to use
     * @param pw          consumer that accepts a PrintWriter for output
     */
    private static void with(final Logger log4jLogger,
                             final Level level,
                             final Consumer<PrintWriter> pw) {
        try (PrintWriter printWriter = new PrintWriter(IoBuilder.forLogger(log4jLogger).
                setLevel(level).
                buildPrintWriter())) {
            pw.accept(printWriter);
        } catch (Exception e) {
            // Silently ignore logging failures
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
     * </ul>
     */
    @Getter
    @RequiredArgsConstructor
    private enum ExitCode {

        /** All tests passed successfully. Exit code: 0 */
        TESTS_PASSED(0, "Tests passed"),

        /** One or more tests failed. Exit code: 1 */
        TESTS_FAILED(1, "Tests failed"),

        /** No tests were found for execution. Exit code: 2 */
        NO_TESTS_FOUND(2, "No tests found");

        private final int exitCode;
        private final String message;
    }

    /**
     * Determines the appropriate exit code based on test execution summary.
     *
     * @param summary the test execution summary
     * @return the exit code corresponding to the test results
     */
    private static @NotNull ExitCode getExitCode(final TestExecutionSummary summary) {
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
        Map<String, UiConfig> uiConfigs =
                ConfigProviderImpl.GlobalTestConfigurationProvider.getUiConfigs();

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

    /**
     * Launches the JUnit Platform and executes all discovered tests.
     *
     * <p>Creates a JUnit launcher, registers a summary listener, executes tests
     * using the configured discovery request, and returns the execution summary.</p>
     *
     * @return the test execution summary containing pass/fail counts and details
     */
    private static TestExecutionSummary launchTestsAndGetSummary() {
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(createRequest());
        return listener.getSummary();
    }

    /**
     * Creates the JUnit launcher discovery request.
     *
     * <p>Configures the request to select {@link RootTest} as the test class
     * and applies parallel execution settings if enabled in the global configuration.</p>
     *
     * @return the configured launcher discovery request
     */
    private static LauncherDiscoveryRequest createRequest() {
        LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request();
        request.selectors(DiscoverySelectors.selectClass(RootTest.class));
        return parallel(request).build();
    }

    /**
     * Configures parallel execution settings for the test launcher.
     *
     * <p>If parallel execution is enabled in the global configuration, configures
     * JUnit Platform to use the custom {@link GlobalParallelExecutionConfigStrategy}.</p>
     *
     * @param b the launcher discovery request builder to configure
     * @return the configured builder with parallel execution settings
     */
    private static LauncherDiscoveryRequestBuilder parallel(final LauncherDiscoveryRequestBuilder b) {
        boolean isParallel = ConfigProviderImpl.GlobalTestConfigurationProvider.provide().isParallelExecution();
        String clazz = GlobalParallelExecutionConfigStrategy.class.getName();
        return b.configurationParameter("junit.jupiter.execution.parallel.enabled", String.valueOf(isParallel))
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "custom")
                .configurationParameter("junit.jupiter.execution.parallel.config.custom.class", clazz);
    }
}
