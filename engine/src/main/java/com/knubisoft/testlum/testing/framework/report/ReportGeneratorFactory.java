package com.knubisoft.testlum.testing.framework.report;

import com.knubisoft.testlum.testing.framework.constant.ExceptionMessage;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.testlum.testing.model.global_config.ExtentReports;
import com.knubisoft.testlum.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.Report;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@UtilityClass
public class ReportGeneratorFactory {

    public ReportGenerator create(final Report report) {
        if (Objects.nonNull(report.getExtentReports())) {
            checkExtentReportsGenerators(report.getExtentReports());
            return new ExtentReportsGenerator();
        } //add a new branch if another implementation is needed
        throw new UnsupportedOperationException("Report generator type is not supported");
    }

    private void checkExtentReportsGenerators(final ExtentReports extentReports) {
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