package com.testlum.report;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.HtmlReport;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.report.extentreports.ExtentReportsConfigurator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ExtentReportsConfigurator} verifying the Spark (HTML) reporter attachment.
 */
@ExtendWith(MockitoExtension.class)
class ExtentReportsConfiguratorTest {

    @Mock
    private GlobalTestConfiguration globalTestConfiguration;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private ExtentReports extentReports;

    private ExtentReportsConfigurator configurator;

    @BeforeEach
    void setUp() {
        configurator = new ExtentReportsConfigurator(globalTestConfiguration, testResourceSettings);
    }

    @Test
    void attachesSparkReporter() {
        setupConfig(false);

        configurator.configure(extentReports);

        verify(extentReports).attachReporter(any(ExtentSparkReporter.class));
    }

    @Test
    void attachesSparkReporterWhenOnlyFailedScenariosAreReported() {
        setupConfig(true);

        configurator.configure(extentReports);

        verify(extentReports).attachReporter(any(ExtentSparkReporter.class));
    }

    private void setupConfig(final boolean onlyFailedScenarios) {
        final HtmlReport html = new HtmlReport();
        html.setEnabled(true);
        final Report report = new Report();
        report.setProjectName("TestProject");
        report.setOnlyFailedScenarios(onlyFailedScenarios);
        report.setHtmlReport(html);
        when(globalTestConfiguration.getReport()).thenReturn(report);
        when(testResourceSettings.getTestResourcesFolder()).thenReturn(new File("/tmp/test-resources"));
    }
}
