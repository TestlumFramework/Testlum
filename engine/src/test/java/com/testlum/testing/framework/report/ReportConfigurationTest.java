package com.testlum.testing.framework.report;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.testlum.testing.model.global_config.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
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
    private JUnitReportGenerator junitReportGenerator;
    @Mock
    private GlobalTestConfiguration globalTestConfiguration;

    private ReportConfiguration reportConfiguration;

    @BeforeEach
    void setUp() {
        reportConfiguration = new ReportConfiguration(extentReportsGenerator, junitReportGenerator,
                globalTestConfiguration);
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
        void runsBothGeneratorsWhenJunitEnabled() {
            final Report report = reportWithHtml();
            final JunitReport junit = new JunitReport();
            junit.setEnabled(true);
            report.setJunit(junit);
            when(globalTestConfiguration.getReport()).thenReturn(report);
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();

            reportConfiguration.create().generateReport(collector);

            verify(extentReportsGenerator).generateReport(collector);
            verify(junitReportGenerator).generateReport(collector);
        }

        @Test
        void removesTheOldJunitReportBeforeTheRunAndFailsIfItCannot() {
            final Report report = reportWithHtml();
            final JunitReport junit = new JunitReport();
            junit.setEnabled(true);
            report.setJunit(junit);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            reportConfiguration.create();
            verify(junitReportGenerator).removeOldReport();

            doThrow(new IllegalStateException("locked")).when(junitReportGenerator).removeOldReport();
            assertThrows(IllegalStateException.class, () -> reportConfiguration.create());
        }

        @Test
        void writesTheJunitFileEvenWhenTheHtmlReportFailsAndStillFailsTheRun() {
            final Report report = reportWithHtml();
            final JunitReport junit = new JunitReport();
            junit.setEnabled(true);
            report.setJunit(junit);
            when(globalTestConfiguration.getReport()).thenReturn(report);
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            doThrow(new IllegalStateException("html broke")).when(extentReportsGenerator).generateReport(collector);

            final ReportGenerator generator = reportConfiguration.create();
            final IllegalStateException thrown =
                    assertThrows(IllegalStateException.class, () -> generator.generateReport(collector));

            assertEquals("html broke", thrown.getMessage());
            verify(junitReportGenerator).generateReport(collector);
        }

        @Test
        void aJunitFailureReachesTheCaller() {
            final Report report = reportWithHtml();
            final JunitReport junit = new JunitReport();
            junit.setEnabled(true);
            report.setJunit(junit);
            when(globalTestConfiguration.getReport()).thenReturn(report);
            final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
            doThrow(new IllegalStateException("junit broke")).when(junitReportGenerator).generateReport(collector);

            final ReportGenerator generator = reportConfiguration.create();

            assertThrows(IllegalStateException.class, () -> generator.generateReport(collector));
        }

        @Test
        void skipsJunitWhenSwitchedOff() {
            final Report report = reportWithHtml();
            final JunitReport junit = new JunitReport();
            junit.setEnabled(false);
            report.setJunit(junit);
            when(globalTestConfiguration.getReport()).thenReturn(report);

            assertSame(extentReportsGenerator, reportConfiguration.create());
        }

        private Report reportWithHtml() {
            final Report report = new Report();
            final ExtentReports extentReports = new ExtentReports();
            final HtmlReportGenerator html = new HtmlReportGenerator();
            html.setEnabled(true);
            extentReports.setHtmlReportGenerator(html);
            report.setExtentReports(extentReports);
            return report;
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
