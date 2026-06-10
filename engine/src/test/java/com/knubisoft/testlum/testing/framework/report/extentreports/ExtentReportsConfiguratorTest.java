package com.knubisoft.testlum.testing.framework.report.extentreports;

import com.aventstack.extentreports.ExtentReports;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ExtentReportsConfigurator} verifying reporter
 * attachment based on configuration settings.
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

    private void setupConfig(final boolean htmlEnabled,
                             final KlovServerReportGenerator klov) {
        final Report report = new Report();
        final com.knubisoft.testlum.testing.model.global_config.ExtentReports erConfig =
                new com.knubisoft.testlum.testing.model.global_config.ExtentReports();
        erConfig.setProjectName("TestProject");
        final HtmlReportGenerator html = new HtmlReportGenerator();
        html.setEnabled(htmlEnabled);
        erConfig.setHtmlReportGenerator(html);
        erConfig.setOnlyFailedScenarios(true);
        erConfig.setKlovServerReportGenerator(klov);
        report.setExtentReports(erConfig);
        when(globalTestConfiguration.getReport()).thenReturn(report);
    }

    @Nested
    class ConfigureSparkReporter {

        @Test
        void attachesSparkReporterWhenHtmlEnabled() {
            setupConfig(true, null);
            final File resourcesFolder = new File("/tmp/test-resources");
            when(testResourceSettings.getTestResourcesFolder()).thenReturn(resourcesFolder);

            configurator.configure(extentReports);

            verify(extentReports).attachReporter(
                    any(com.aventstack.extentreports.reporter.ExtentSparkReporter.class));
        }

        @Test
        void doesNotAttachSparkReporterWhenHtmlDisabled() {
            setupConfig(false, null);

            configurator.configure(extentReports);

            verify(extentReports, never()).attachReporter(any());
        }
    }

    @Nested
    class ConfigureKlovReporter {

        @Test
        void doesNotAttachKlovWhenNull() {
            setupConfig(false, null);

            configurator.configure(extentReports);

            verify(extentReports, never()).attachReporter(any());
        }

        @Test
        void doesNotAttachKlovWhenDisabled() {
            final KlovServerReportGenerator klov = new KlovServerReportGenerator();
            klov.setEnabled(false);
            setupConfig(false, klov);

            configurator.configure(extentReports);

            verify(extentReports, never()).attachReporter(any());
        }
    }
}
