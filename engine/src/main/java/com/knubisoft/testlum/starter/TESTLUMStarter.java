package com.knubisoft.testlum.starter;

import com.knubisoft.testlum.testing.RootTest;
import com.knubisoft.testlum.testing.framework.SystemPropertiesHelper;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.util.ArgumentsUtils;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.junit.platform.engine.discovery.DiscoverySelectors;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@SpringBootApplication
public class TESTLUMStarter {

    private static final String PARALLEL = "junit.jupiter.execution.parallel.enabled";
    private static final String STRATEGY = "junit.jupiter.execution.parallel.config.strategy";
    private static final String CLASS = "junit.jupiter.execution.parallel.config.custom.class";
    private static final String JUNIT_STRATEGY_CUSTOM = "custom";
    private static final String JUNIT_PARALLEL_CONFIG =
            "com.knubisoft.testlum.testing.framework.env.parallel.GlobalParallelExecutionConfigStrategy";

    public static void main(final String[] args) {
        Optional<String> configFileName = ArgumentsUtils.getConfigurationFileName(safeGet(args, 0));
        Optional<String> pathToTestResources = ArgumentsUtils.getPathToTestResources(safeGet(args, 1));
        Optional<String> scenarioScope = ArgumentsUtils.getScenarioScope(safeGet(args, 2));
        System.setProperty("resource", safeGet(args, 1).orElse(StringUtils.EMPTY));

        TestResourceSettings.init(
                configFileName.orElseThrow(() ->
                        new IllegalArgumentException("No config file provided as first argument")),
                pathToTestResources.orElseThrow(() ->
                        new IllegalArgumentException("No path to test resources provided as second argument")),
                scenarioScope);

        initLocatorsFolder();
        TestExecutionSummary testExecutionSummary = runTests();

        LogUtil.logTestExecutionSummary(testExecutionSummary);

        if (SystemPropertiesHelper.isTestingInPipeline()) {
            ResultUtil.writeFullTestCycleExecutionResult(testExecutionSummary);
        }
    }

    private static Optional<String> safeGet(final String[] args, final int index) {
        try {
            return Optional.of(args[index]);
        } catch (IndexOutOfBoundsException e) {
            return Optional.empty();
        }
    }

    private static void initLocatorsFolder() {
        Map<String, UiConfig> configs = ConfigProviderImpl.GlobalTestConfigurationProvider.getUiConfigs();
        boolean isUiConfigEnabled = configs.values().stream().anyMatch(TESTLUMStarter::isUiConfigEnabled);
        if (isUiConfigEnabled) {
            TestResourceSettings.getInstance().initLocatorsFolder();
        }
    }

    private static boolean isUiConfigEnabled(final UiConfig uiConfig) {
        List<Boolean> configParams = Arrays.asList(
                Optional.ofNullable(uiConfig.getWeb()).map(Web::isEnabled).orElse(false),
                Optional.ofNullable(uiConfig.getNative()).map(Native::isEnabled).orElse(false),
                Optional.ofNullable(uiConfig.getMobilebrowser()).map(Mobilebrowser::isEnabled).orElse(false)
        );
        return configParams.contains(Boolean.TRUE);
    }

    private static TestExecutionSummary runTests() {
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(createLaunchRequest());
        return listener.getSummary();
    }

    private static @NotNull LauncherDiscoveryRequest createLaunchRequest() {
        GlobalTestConfiguration config = ConfigProviderImpl.GlobalTestConfigurationProvider.provide();
        return LauncherDiscoveryRequestBuilder
                .request()
                .selectors(DiscoverySelectors.selectClass(RootTest.class))
                .configurationParameter(PARALLEL,
                        String.valueOf(config.isParallelExecution()))
                .configurationParameter(STRATEGY, JUNIT_STRATEGY_CUSTOM)
                .configurationParameter(CLASS, JUNIT_PARALLEL_CONFIG)
                .build();
    }
}
