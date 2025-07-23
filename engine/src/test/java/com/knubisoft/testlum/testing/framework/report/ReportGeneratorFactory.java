package com.knubisoft.testlum.testing.framework.report;

import com.knubisoft.testlum.testing.framework.report.extentreports.ExtentReportsGenerator;
import com.knubisoft.testlum.testing.framework.testRail.TestRailService;
import com.knubisoft.testlum.testing.model.global_config.ExtentReports;
import com.knubisoft.testlum.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.Report;
import com.knubisoft.testlum.testing.model.global_config.TestRailReports;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_REPORT_GENERATOR_ENABLED;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.NO_REPORT_GENERATOR_PRESENT;

@Slf4j
@UtilityClass
public class ReportGeneratorFactory {

    public ReportGenerator create(final Report report, final TestRailService testRailService) {
        if (Objects.nonNull(report.getExtentReports())) {
            checkExtentReportsGenerators(report.getExtentReports());
        } else { //add a new branch if another implementation is needed
            log.warn(NO_REPORT_GENERATOR_PRESENT);
        }
        ExtentReportsGenerator reportsGenerator = new ExtentReportsGenerator();
        reportsGenerator.setTestRailService(testRailService);
        return reportsGenerator;
    }

    private void checkExtentReportsGenerators(final ExtentReports extentReports) {
        HtmlReportGenerator htmlReportGenerator = extentReports.getHtmlReportGenerator();
        KlovServerReportGenerator klovServerReportGenerator = extentReports.getKlovServerReportGenerator();
        TestRailReports testRailReports = extentReports.getTestRailReports();

        boolean isHtmlReportGeneratorDisabled = Objects.isNull(htmlReportGenerator) || !htmlReportGenerator.isEnabled();
        boolean isKlovServerReportGeneratorDisabled = Objects.isNull(klovServerReportGenerator) || !klovServerReportGenerator.isEnabled();
        boolean isTestRailReportGeneratorDisabled = Objects.isNull(testRailReports) || !testRailReports.isEnabled();

        if (isHtmlReportGeneratorDisabled && isKlovServerReportGeneratorDisabled && isTestRailReportGeneratorDisabled) {
            log.warn(NO_REPORT_GENERATOR_ENABLED);
        }
    }
}
