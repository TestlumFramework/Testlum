package com.knubisoft.e2e.runner;

import com.knubisoft.e2e.testing.E2ERootTest;
import com.knubisoft.e2e.testing.framework.SystemInfo;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.util.ArgumentsUtils;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.framework.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;


import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Slf4j
public class TestRunner {

    public static void main(final String[] args) {
        ArgumentsUtils.validateInputArguments(args);
        String configFileName = ArgumentsUtils.getConfigurationFileName(args[0]);
        String pathToTestResources = ArgumentsUtils.getPathToTestResources(args[1]);
        TestResourceSettings.init(configFileName, pathToTestResources);
        TestExecutionSummary testExecutionSummary = runTests();
        if (SystemInfo.TESTING_IN_PIPELINE) {
            ResultUtil.SetFullTestCycleExecutionResult(testExecutionSummary);
        }
    }

    private static TestExecutionSummary runTests() {
        LauncherDiscoveryRequest tests = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectClass(E2ERootTest.class))
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(tests);
        TestExecutionSummary testExecutionSummary = listener.getSummary();
        LogUtil.logTestExecutionSummary(testExecutionSummary);
        return testExecutionSummary;
    }
}
