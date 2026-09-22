package com.testlum.report;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.HtmlReport;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.model.global_config.TestlumReportServer;
import com.testlum.testing.report.ReportConfiguration;
import com.testlum.testing.report.extentreports.ExtentHtmlReportListener;
import com.testlum.testing.report.server.TestlumServerReportListener;
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
    private ExtentHtmlReportListener htmlListener;
    @Mock
    private TestlumServerReportListener serverListener;

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

    @Test
    void createsNoListenerWithoutReportSection() {
        when(globalTestConfiguration.getReport()).thenReturn(null);

        reportConfiguration.reportingService(globalTestConfiguration, htmlProvider, serverProvider).onRunStart();

        verify(htmlProvider, never()).getObject();
        verify(serverProvider, never()).getObject();
    }

    @Test
    void createsOnlyHtmlListenerWhenServerIsAbsent() {
        when(globalTestConfiguration.getReport()).thenReturn(report(true, null));
        when(htmlProvider.getObject()).thenReturn(htmlListener);

        reportConfiguration.reportingService(globalTestConfiguration, htmlProvider, serverProvider).onRunStart();

        verify(htmlListener).onRunStart();
        verify(serverProvider, never()).getObject();
    }

    @Test
    void createsBothListenersWhenBothAreEnabled() {
        when(globalTestConfiguration.getReport()).thenReturn(report(true, server(null)));
        when(htmlProvider.getObject()).thenReturn(htmlListener);
        when(serverProvider.getObject()).thenReturn(serverListener);

        reportConfiguration.reportingService(globalTestConfiguration, htmlProvider, serverProvider).onRunStart();

        verify(htmlListener).onRunStart();
        verify(serverListener).onRunStart();
    }

    @Test
    void skipsDisabledListeners() {
        when(globalTestConfiguration.getReport()).thenReturn(report(false, server(false)));

        reportConfiguration.reportingService(globalTestConfiguration, htmlProvider, serverProvider).onRunStart();

        verify(htmlProvider, never()).getObject();
        verify(serverProvider, never()).getObject();
    }

    @Test
    void failsFastWhenServerListenerCannotBeCreated() {
        when(globalTestConfiguration.getReport()).thenReturn(report(false, server(true)));
        when(serverProvider.getObject()).thenThrow(new DefaultFrameworkException("broker unreachable"));

        assertThrows(DefaultFrameworkException.class,
                () -> reportConfiguration.reportingService(globalTestConfiguration, htmlProvider, serverProvider));
    }
}
