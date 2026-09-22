package com.testlum.testing.report;

import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.HtmlReport;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.model.global_config.TestlumReportServer;
import com.testlum.testing.report.extentreports.ExtentHtmlReportListener;
import com.testlum.testing.report.server.TestlumServerReportListener;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ReportConfiguration {

    @Bean
    public ReportingService reportingService(final GlobalTestConfiguration globalTestConfiguration,
                                             final ObjectProvider<ExtentHtmlReportListener> htmlListener,
                                             final ObjectProvider<TestlumServerReportListener> serverListener) {
        Report report = globalTestConfiguration.getReport();
        List<ReportListener> listeners = new ArrayList<>();
        if (report != null && isEnabled(report.getHtmlReport())) {
            listeners.add(htmlListener.getObject());
        }
        if (report != null && isEnabled(report.getTestlumReportServer())) {
            listeners.add(serverListener.getObject());
        }
        return new ReportingService(listeners);
    }

    private boolean isEnabled(final HtmlReport htmlReport) {
        return htmlReport != null && htmlReport.isEnabled();
    }

    private boolean isEnabled(final TestlumReportServer reportServer) {
        return reportServer != null && reportServer.isEnabled();
    }
}
