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

public class TESTLUMStarter {

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

    private static void formatMessageAndExitCode(final TestExecutionSummary summary) {
        Result result = getResult(summary);
        System.out.println("Execution status [" + result.message() + "]");
        summary.printTo(new PrintWriter(System.out));
        summary.printFailuresTo(new PrintWriter(System.out));
        System.exit(result.exitCode());
    }

    private static @NotNull Result getResult(final TestExecutionSummary summary) {
        final int exitCode;
        final String message;
        if (summary.getTestsFoundCount() == 0) {
            exitCode = 2;
            message = "No tests found";
        } else if (summary.getTestsFailedCount() > 0) {
            exitCode = 1;
            message = "Tests failed";
        } else {
            exitCode = 0;
            message = "Tests passed";
        }
        return new Result(exitCode, message);
    }

    private record Result(int exitCode, String message) {
    }

    private static void initLocatorsFolder() {
        Map<String, UiConfig> uiConfigs = ConfigProviderImpl.GlobalTestConfigurationProvider.getUiConfigs();

        boolean isUiConfigEnabled = uiConfigs.values().stream().anyMatch(TESTLUMStarter::isUiConfigEnabled);

        if (isUiConfigEnabled) {
            TestResourceSettings.getInstance().initLocatorsFolder();
        }
    }

    private static boolean isUiConfigEnabled(final UiConfig uiConfig) {
        boolean webEnabled = Optional.ofNullable(uiConfig.getWeb()).
                map(Web::isEnabled).orElse(false);
        boolean nativeEnabled = Optional.ofNullable(uiConfig.getNative()).
                map(Native::isEnabled).orElse(false);
        boolean mobileEnabled = Optional.ofNullable(uiConfig.getMobilebrowser()).
                map(Mobilebrowser::isEnabled).orElse(false);
        return webEnabled || nativeEnabled || mobileEnabled;
    }

    private static TestExecutionSummary launchTestsAndGetSummary() {
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(createRequest());
        return listener.getSummary();
    }

    private static LauncherDiscoveryRequest createRequest() {
        LauncherDiscoveryRequestBuilder request = LauncherDiscoveryRequestBuilder.request();
        request.selectors(DiscoverySelectors.selectClass(RootTest.class));
        return parallel(request).build();
    }

    private static LauncherDiscoveryRequestBuilder parallel(final LauncherDiscoveryRequestBuilder b) {
        boolean isParallel = ConfigProviderImpl.GlobalTestConfigurationProvider.provide().isParallelExecution();
        String clazz = GlobalParallelExecutionConfigStrategy.class.getName();
        return b.configurationParameter("junit.jupiter.execution.parallel.enabled", String.valueOf(isParallel))
                .configurationParameter("junit.jupiter.execution.parallel.config.strategy", "custom")
                .configurationParameter("junit.jupiter.execution.parallel.config.custom.class", clazz);
    }
}
