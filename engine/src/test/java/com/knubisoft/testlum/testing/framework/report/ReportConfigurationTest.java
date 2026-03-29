package com.knubisoft.testlum.testing.framework.report;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link ReportConfiguration} verifying bean creation
 * and report validation logic.
 */
@ExtendWith(MockitoExtension.class)
class ReportConfigurationTest {

    @Mock
    private ExtentReportsGenerator extentReportsGenerator;
    @Mock
    private GlobalTestConfiguration globalTestConfiguration;

    private ReportConfiguration reportConfiguration;

    @BeforeEach
    void setUp() {
        reportConfiguration = new ReportConfiguration(extentReportsGenerator, globalTestConfiguration);
    }

    @Nested
    class CreateBean {

        @Test
        void returnsExtentReportsGeneratorWhenReportEnabled() {
            final Report report = new Report();
            final ExtentReports extentReports = new ExtentReports();
            final HtmlReportGenerator html = new HtmlReportGenerator();
            html.setEnabled(true);
            extentReports.setHtmlReportGenerator(html);
            report.setExtentReports(extentReports);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            final ReportGenerator result = reportConfiguration.create();

            assertSame(extentReportsGenerator, result);
            assertInstanceOf(ExtentReportsGenerator.class, result);
        }

        @Test
        void throwsWhenReportIsNull() {
            when(globalTestConfiguration.getReport()).thenReturn(null);

            assertThrows(UnsupportedOperationException.class, () -> reportConfiguration.create());
        }
    }

    @Nested
    class ValidateReport {

        @Test
        void succeedsWhenHtmlReporterEnabled() {
            final Report report = new Report();
            final ExtentReports extentReports = new ExtentReports();
            final HtmlReportGenerator html = new HtmlReportGenerator();
            html.setEnabled(true);
            extentReports.setHtmlReportGenerator(html);
            report.setExtentReports(extentReports);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            final ReportGenerator result = reportConfiguration.create();
            assertSame(extentReportsGenerator, result);
        }

        @Test
        void succeedsWhenOnlyKlovEnabled() {
            final Report report = new Report();
            final ExtentReports extentReports = new ExtentReports();
            final HtmlReportGenerator html = new HtmlReportGenerator();
            html.setEnabled(false);
            final KlovServerReportGenerator klov = new KlovServerReportGenerator();
            klov.setEnabled(true);
            extentReports.setHtmlReportGenerator(html);
            extentReports.setKlovServerReportGenerator(klov);
            report.setExtentReports(extentReports);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            final ReportGenerator result = reportConfiguration.create();
            assertSame(extentReportsGenerator, result);
        }

        @Test
        void throwsWhenNoReportersEnabled() {
            final Report report = new Report();
            final ExtentReports extentReports = new ExtentReports();
            final HtmlReportGenerator html = new HtmlReportGenerator();
            html.setEnabled(false);
            extentReports.setHtmlReportGenerator(html);
            extentReports.setKlovServerReportGenerator(null);
            report.setExtentReports(extentReports);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            assertThrows(DefaultFrameworkException.class, () -> reportConfiguration.create());
        }

        @Test
        void throwsWhenKlovExistsButDisabled() {
            final Report report = new Report();
            final ExtentReports extentReports = new ExtentReports();
            final HtmlReportGenerator html = new HtmlReportGenerator();
            html.setEnabled(false);
            final KlovServerReportGenerator klov = new KlovServerReportGenerator();
            klov.setEnabled(false);
            extentReports.setHtmlReportGenerator(html);
            extentReports.setKlovServerReportGenerator(klov);
            report.setExtentReports(extentReports);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            assertThrows(DefaultFrameworkException.class, () -> reportConfiguration.create());
        }
    }
}
