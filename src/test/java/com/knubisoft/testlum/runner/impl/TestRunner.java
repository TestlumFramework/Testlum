package com.knubisoft.testlum.runner.impl;

import com.knubisoft.testlum.runner.Runner;
import com.knubisoft.testlum.testing.RootTest;
import com.knubisoft.testlum.testing.framework.SystemInfo;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.util.ArgumentsUtils;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.IOException;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

@Slf4j
public class TestRunner implements Runner {

    private static final String JUNIT_PROPERTIES_FILENAME = "junit-platform.properties";
    private static final String PARALLEL_ENABLED = "junit.jupiter.execution.parallel.enabled=true";
    private static final String PARALLEL_DISABLED = "junit.jupiter.execution.parallel.enabled=false";

    @Override
    public void run(final String[] args) {
        String configFileName = ArgumentsUtils.getConfigurationFileName(args[0]);
        String pathToTestResources = ArgumentsUtils.getPathToTestResources(args[1]);
        TestResourceSettings.init(configFileName, pathToTestResources);
        isParallelExecution();
        TestExecutionSummary testExecutionSummary = runTests();
        LogUtil.logTestExecutionSummary(testExecutionSummary);
        if (SystemInfo.TESTING_IN_PIPELINE) {
            ResultUtil.writeFullTestCycleExecutionResult(testExecutionSummary);
        }
    }

    private void isParallelExecution() {
        if (!GlobalTestConfigurationProvider.provide().isParallelExecution()) {
            try {
                File junitPropertiesFile = new File(ClassLoader.getSystemResource(JUNIT_PROPERTIES_FILENAME).getFile());
                String junitProperties = FileUtils.readFileToString(junitPropertiesFile, UTF_8);
                String newJunitProperties = junitProperties.replace(PARALLEL_ENABLED, PARALLEL_DISABLED);
                FileUtils.write(junitPropertiesFile, newJunitProperties, UTF_8);
            } catch (IOException e) {
                log.error(e.getMessage());
            }
        }
    }

    private TestExecutionSummary runTests() {
        LauncherDiscoveryRequest tests = LauncherDiscoveryRequestBuilder
                .request()
                .selectors(selectClass(RootTest.class))
                .build();
        Launcher launcher = LauncherFactory.create();
        SummaryGeneratingListener listener = new SummaryGeneratingListener();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(tests);
        return listener.getSummary();
    }
}
