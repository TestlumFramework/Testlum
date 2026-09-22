package com.testlum.report;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.HtmlReport;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.model.global_config.TestRailReport;
import com.testlum.testing.model.global_config.TestlumReportServer;
import com.testlum.testing.report.GlobalScenarioStatCollector;
import com.testlum.testing.report.ReportConfiguration;
import com.testlum.testing.report.ReportingService;
import com.testlum.testing.report.extentreports.ExtentHtmlReportListener;
import com.testlum.testing.report.server.TestlumServerReportListener;
import com.testlum.testing.report.testrail.TestRailReportListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReportConfiguration} verifying that only the enabled report listeners are created.
 */
@ExtendWith(MockitoExtension.class)
class ReportConfigurationTest {

    private final ReportConfiguration reportConfiguration = new ReportConfiguration();

    @Mock
    private GlobalTestConfiguration globalTestConfiguration;
    @Mock
    private ObjectProvider<ExtentHtmlReportListener> htmlProvider;
    @Mock
    private ObjectProvider<TestlumServerReportListener> serverProvider;
    @Mock
    private ObjectProvider<TestRailReportListener> testRailProvider;
    @Mock
    private ExtentHtmlReportListener htmlListener;
    @Mock
    private TestlumServerReportListener serverListener;
    @Mock
    private TestRailReportListener testRailListener;

    private static Report report(final boolean htmlEnabled, final TestlumReportServer server) {
        final HtmlReport html = new HtmlReport();
        html.setEnabled(htmlEnabled);
        final Report report = new Report();
        report.setProjectName("TestProject");
        report.setHtmlReport(html);
        report.setTestlumReportServer(server);
        return report;
    }

    private static TestlumReportServer server(final Boolean enabled) {
        final TestlumReportServer server = new TestlumReportServer();
        server.setEnabled(enabled);
        return server;
    }

    private ReportingService reportingService() {
        return reportConfiguration.reportingService(globalTestConfiguration, htmlProvider, serverProvider,
                testRailProvider);
    }

    private static TestRailReport testRail(final boolean enabled) {
        final TestRailReport testRail = new TestRailReport();
        testRail.setEnabled(enabled);
        return testRail;
    }

    @Test
    void createsNoListenerWithoutReportSection() {
        when(globalTestConfiguration.getReport()).thenReturn(null);

        reportingService().onRunStart();

        verify(htmlProvider, never()).getObject();
        verify(serverProvider, never()).getObject();
        verify(testRailProvider, never()).getObject();
    }

    @Test
    void createsOnlyHtmlListenerWhenServerIsAbsent() {
        when(globalTestConfiguration.getReport()).thenReturn(report(true, null));
        when(htmlProvider.getObject()).thenReturn(htmlListener);

        reportingService().onRunStart();

        verify(htmlListener).onRunStart();
        verify(serverProvider, never()).getObject();
    }

    @Test
    void createsBothListenersWhenBothAreEnabled() {
        when(globalTestConfiguration.getReport()).thenReturn(report(true, server(null)));
        when(htmlProvider.getObject()).thenReturn(htmlListener);
        when(serverProvider.getObject()).thenReturn(serverListener);

        reportingService().onRunStart();

        verify(htmlListener).onRunStart();
        verify(serverListener).onRunStart();
    }

    @Test
    void skipsDisabledListeners() {
        when(globalTestConfiguration.getReport()).thenReturn(report(false, server(false)));

        reportingService().onRunStart();

        verify(htmlProvider, never()).getObject();
        verify(serverProvider, never()).getObject();
    }

    @Test
    void createsTestRailListenerWhenTestRailIsEnabled() {
        final Report report = report(false, null);
        report.setTestRail(testRail(true));
        when(globalTestConfiguration.getReport()).thenReturn(report);
        when(testRailProvider.getObject()).thenReturn(testRailListener);
        final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();

        reportingService().onRunFinished(collector);

        verify(testRailListener).onRunFinished(collector);
        verify(htmlProvider, never()).getObject();
        verify(serverProvider, never()).getObject();
    }

    @Test
    void skipsDisabledTestRailListener() {
        final Report report = report(true, null);
        report.setTestRail(testRail(false));
        when(globalTestConfiguration.getReport()).thenReturn(report);
        when(htmlProvider.getObject()).thenReturn(htmlListener);

        reportingService().onRunStart();

        verify(testRailProvider, never()).getObject();
    }

    @Test
    void failsFastWhenServerListenerCannotBeCreated() {
        when(globalTestConfiguration.getReport()).thenReturn(report(false, server(true)));
        when(serverProvider.getObject()).thenThrow(new DefaultFrameworkException("broker unreachable"));

        assertThrows(DefaultFrameworkException.class,
                () -> reportingService());
    }
}
