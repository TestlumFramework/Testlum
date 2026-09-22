package com.testlum.testing.report.extentreports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.testlum.testing.framework.TestResourceSettings;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Slf4j
@RequiredArgsConstructor
@Component
public class ExtentReportsConfigurator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyyТHH.mm.ss");
    private static final String PATH_FOR_REPORT_FOLDER = "%s%s%s";
    private static final String REPORT_NAME_TEMPLATE = "%s%s_%s.html";
    private static final String TEMPLATE_FOR_REPORT_SAVING_PATH = "%s%s%s%s";

    private final GlobalTestConfiguration globalTestConfiguration;
    private final TestResourceSettings testResourceSettings;

    public void configure(final ExtentReports extentReports) {
        attachSparkReporter(extentReports, globalTestConfiguration.getReport().getProjectName());
    }

    private void attachSparkReporter(final ExtentReports extentReports, final String projectName) {
        String reportPath = buildReportPath(projectName);
        try {
            ExtentSparkReporter sparkReporter = new ExtentSparkReporter(reportPath);
            if (globalTestConfiguration.getReport().isOnlyFailedScenarios()) {
                sparkReporter.filter()
                        .statusFilter()
                        .as(new Status[]{Status.FAIL})
                        .apply();
            }
            extentReports.attachReporter(sparkReporter);
        } catch (Exception e) {
            log.error("Unable to create report file by path: {}", reportPath);
        }
    }

    private String buildReportPath(final String projectName) {
        LocalDateTime dateTime = LocalDateTime.now();
        String pathForReportFolder = String.format(PATH_FOR_REPORT_FOLDER,
                testResourceSettings.getTestResourcesFolder().getAbsolutePath(),
                File.separator, TestResourceSettings.REPORT_FOLDER);
        String reportName = String.format(REPORT_NAME_TEMPLATE, File.separator,
                projectName, dateTime.format(DATE_TIME_FORMATTER));
        return String.format(TEMPLATE_FOR_REPORT_SAVING_PATH,
                pathForReportFolder, File.separator, dateTime.format(DATE_FORMATTER), reportName);
    }
}
