package com.knubisoft.testlum.testing.framework.report;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.testlum.testing.model.global_config.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReportConfiguration {

    private final ExtentReportsGenerator extentReportsGenerator;
    private final GlobalTestConfiguration globalTestConfiguration;

    @Primary
    @Bean
    public ReportGenerator create() {
        Report report = globalTestConfiguration.getReport();
        if (report != null) {
            validateReport(report.getExtentReports());
            return extentReportsGenerator;
        } else {
            throw new UnsupportedOperationException("Report generator type is not supported");
        }
    }

    private void validateReport(final ExtentReports extentReports) {
        HtmlReportGenerator htmlReportGenerator = extentReports.getHtmlReportGenerator();
        KlovServerReportGenerator klovServerReportGenerator = extentReports.getKlovServerReportGenerator();
        if (!htmlReportGenerator.isEnabled()) {
            if (Objects.isNull(klovServerReportGenerator) || !klovServerReportGenerator.isEnabled()) {
                log.error(ExceptionMessage.NO_ENABLED_REPORT_GENERATORS_FOUND);
                throw new DefaultFrameworkException("At least one report generator must be enabled");
            }
        }
    }
}