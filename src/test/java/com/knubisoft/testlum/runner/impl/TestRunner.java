package com.knubisoft.testlum.runner.impl;

import com.knubisoft.testlum.runner.Runner;
import com.knubisoft.testlum.testing.RootTest;
import com.knubisoft.testlum.testing.framework.SystemInfo;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.util.ArgumentsUtils;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import static com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider.provide;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

public class TestRunner implements Runner {

    private static final String PARALLEL = "junit.jupiter.execution.parallel.enabled";
    private static final String STRATEGY = "junit.jupiter.execution.parallel.config.strategy";
    private static final String CLASS = "junit.jupiter.execution.parallel.config.custom.class";
    private static final String JUNIT_STRATEGY_CUSTOM = "custom";
    private static final String JUNIT_PARALLEL_CONFIG =
            "com.knubisoft.testlum.testing.framework.env.parallel.GlobalParallelExecutionConfigStrategy";

    @Override
    public void run(final String[] args) {
        String configFileName = ArgumentsUtils.getConfigurationFileName(args[0]);
        String pathToTestResources = ArgumentsUtils.getPathToTestResources(args[1]);
        TestResourceSettings.init(configFileName, pathToTestResources);
        TestExecutionSummary testExecutionSummary = runTests();
        LogUtil.logTestExecutionSummary(testExecutionSummary);
        if (SystemInfo.TESTING_IN_PIPELINE) {
            ResultUtil.writeFullTestCycleExecutionResult(testExecutionSummary);
        }
    }

    private TestExecutionSummary runTests() {
        LauncherDiscoveryRequest tests = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectClass(RootTest.class))
                .configurationParameter(PARALLEL, provide().isParallelExecution().toString())
                .configurationParameter(STRATEGY, JUNIT_STRATEGY_CUSTOM)
                .configurationParameter(CLASS, JUNIT_PARALLEL_CONFIG)
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(tests);
        return listener.getSummary();
    }
}
