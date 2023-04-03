package com.knubisoft.cott.testing.framework.report;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.cott.testing.model.global_config.ExtentReports;
import com.knubisoft.cott.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.cott.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.cott.testing.model.global_config.Report;
import lombok.experimental.UtilityClass;

import java.util.Objects;

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
                throw new DefaultFrameworkException("At least one report generator must be enabled");
            }
        }
    }
}
