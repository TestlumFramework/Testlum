package com.knubisoft.testlum.runner.impl;

import com.knubisoft.testlum.runner.Runner;
import com.knubisoft.testlum.testing.RootTest;
import com.knubisoft.testlum.testing.framework.SystemInfo;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.util.ArgumentsUtils;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import lombok.SneakyThrows;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Properties;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

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

    @SneakyThrows
    private void isParallelExecution() {
        if (!GlobalTestConfigurationProvider.provide().isParallelExecution()) {
            Properties properties = new Properties();
            try (InputStream inputStream = ClassLoader.getSystemResourceAsStream(JUNIT_PROPERTIES_FILENAME);
                 OutputStream outputStream = Files.newOutputStream(
                         new File(ClassLoader.getSystemResource(JUNIT_PROPERTIES_FILENAME).toURI()).toPath())) {
                properties.load(inputStream);
                properties.setProperty(PARALLEL_ENABLED, PARALLEL_DISABLED);
                properties.store(outputStream, null);
            } catch (IOException e) {
                //ignore
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
