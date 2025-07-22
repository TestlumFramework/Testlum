package com.knubisoft.testlum.testing.framework.report;

import com.knubisoft.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.testlum.testing.model.global_config.ExtentReports;
import com.knubisoft.testlum.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.Report;
import com.knubisoft.testlum.testing.model.global_config.TestRailReports;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
@UtilityClass
public class ReportGeneratorFactory {

    public ReportGenerator create(final Report report) {
        if (Objects.nonNull(report.getExtentReports())) {
            checkExtentReportsGenerators(report.getExtentReports());
        } else { //add a new branch if another implementation is needed
            log.warn("No Report Generator is present");
        }
        return new ExtentReportsGenerator();
    }

    private void checkExtentReportsGenerators(final ExtentReports extentReports) {
        HtmlReportGenerator htmlReportGenerator = extentReports.getHtmlReportGenerator();
        KlovServerReportGenerator klovServerReportGenerator = extentReports.getKlovServerReportGenerator();
        TestRailReports testRailReports = extentReports.getTestRailReports();
        if ((Objects.isNull(htmlReportGenerator) || !htmlReportGenerator.isEnabled()) &&
             (Objects.isNull(klovServerReportGenerator) || !klovServerReportGenerator.isEnabled()) &&
              (Objects.isNull(testRailReports) || !testRailReports.isEnabled())) {
                    log.warn("No Report Generator is enabled");
            }
    }
}
