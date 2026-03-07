package com.knubisoft.testlum.starter;

import com.knubisoft.testlum.testing.RootTest;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.env.parallel.GlobalParallelExecutionConfigStrategy;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Component
public class TestRunner implements CommandLineRunner {

    private final XMLParsers xmlParsers;
    private final TestResourceSettings testResourceSettings;

    @Override
    public void run(final String... args) {
        TestExecutionSummary summary = launchTestsAndGetSummary();
        formatMessageAndExitCode(summary);
    }

    /**
     * Launches the JUnit Platform and executes all discovered tests.
     *
     * <p>Creates a JUnit launcher, registers a summary listener, executes tests
     * using the configured discovery request, and returns the execution summary.</p>
     *
     * @return the test execution summary containing pass/fail counts and details
     */
    private TestExecutionSummary launchTestsAndGetSummary() {
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
    private LauncherDiscoveryRequest createRequest() {
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
    private LauncherDiscoveryRequestBuilder parallel(final LauncherDiscoveryRequestBuilder b) {
        GlobalTestConfiguration globalTestConfiguration =
                xmlParsers.forGlobalTestConfiguration().process(testResourceSettings.getConfigFile());
        String clazz = GlobalParallelExecutionConfigStrategy.class.getName();
        return b.configurationParameter("junit.jupiter.execution.parallel.enabled",
                        String.valueOf(this.isParallel(globalTestConfiguration)))
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "custom")
                .configurationParameter("junit.jupiter.execution.parallel.config.custom.class", clazz)
                .configurationParameter("junit.jupiter.execution.parallel.config.testlum.parallelism",
                        String.valueOf(this.computeParallelism(globalTestConfiguration)));
    }

    private boolean isParallel(final GlobalTestConfiguration globalTestConfiguration) {
        return Boolean.TRUE.equals(globalTestConfiguration.isParallelExecution());
    }

    private int computeParallelism(final GlobalTestConfiguration globalTestConfiguration) {
        int envThreads = globalTestConfiguration.getEnvironments().getEnv()
                .stream()
                .mapToInt(Environment::getThreads)
                .sum();
        int cpu = Runtime.getRuntime().availableProcessors();
        return Math.max(1, Math.min(envThreads, cpu * 2));
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
        TESTLUMStarter.ExitCode exitCode = TESTLUMStarter.getExitCode(summary);

        log.info("Execution status [{}]", exitCode.getMessage());
        log.info(toString(summary::printTo).lines().filter(line ->
                        !line.contains("container"))
                .collect(Collectors.joining(System.lineSeparator())));
        if (summary.getTestsFailedCount() > 0 || summary.getFailures() != null && !summary.getFailures().isEmpty()) {
            log.error(toString(summary::printFailuresTo));
        }

        System.exit(exitCode.getExitCode());
    }

    /**
     * Converts a PrintWriter consumer output to a String.
     *
     * @param writer consumer that writes to a PrintWriter
     * @return the written content as a string
     */
    private static String toString(final Consumer<PrintWriter> writer) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        writer.accept(printWriter);
        return stringWriter.toString();
    }
}
