package com.knubisoft.e2e.testing.framework.report;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.e2e.testing.model.global_config.ExtentReports;
import com.knubisoft.e2e.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.e2e.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.e2e.testing.model.global_config.Report;
import lombok.experimental.UtilityClass;

import java.util.Objects;

@UtilityClass
public class ReportGeneratorFactory {

    public ReportGenerator create(Report report) {
        if (Objects.nonNull(report.getExtentReports())) {
            checkExtentReportsGenerators(report.getExtentReports());
            return new ExtentReportsGenerator();
        }
        throw new UnsupportedOperationException("Report generator type is not supported");
    }

    private void checkExtentReportsGenerators(final ExtentReports extentReports) {
        HtmlReportGenerator htmlReportGenerator = extentReports.getHtmlReportGenerator();
        KlovServerReportGenerator klovServerReportGenerator = extentReports.getKlovServerReportGenerator();
        if (!htmlReportGenerator.isEnable()) {
            if (klovServerReportGenerator == null || !klovServerReportGenerator.isEnable()) {
                throw new DefaultFrameworkException("At least one report generator must be enabled");
            }
        }
    }
}
