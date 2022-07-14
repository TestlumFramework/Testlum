package com.knubisoft.e2e.runner;

import com.knubisoft.e2e.testing.E2ERootTest;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.util.ArgumentsUtils;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;


import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXECUTION_RESULT;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Slf4j
public class TestRunner {

    private static final String SUCCESS_PROPERTY_KEY = "all.scenarios.passed.successfully";

    public static void main(final String[] args) {
        ArgumentsUtils.validateInputArguments(args);
        String configFileName = ArgumentsUtils.getConfigurationFileName(args[0]);
        String pathToTestResources = ArgumentsUtils.getPathToTestResources(args[1]);
        TestResourceSettings.init(configFileName, pathToTestResources);
        TestExecutionSummary testExecutionSummary = runTests();
        setExecutionResult(testExecutionSummary);
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

    private static void setExecutionResult(final TestExecutionSummary testExecutionSummary) {
        String executionResult = testExecutionSummary.getFailures().isEmpty()
                ? System.setProperty(SUCCESS_PROPERTY_KEY, "true") : System.setProperty(SUCCESS_PROPERTY_KEY, "false");
        log.info(EXECUTION_RESULT, executionResult);
    }
}
