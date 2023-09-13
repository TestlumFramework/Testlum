package com.knubisoft.testlum.testing.framework.report.extentreports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentKlovReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.testlum.testing.model.global_config.Mongodb;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import static java.io.File.separator;
import static java.lang.String.format;

@Slf4j
@UtilityClass
public class ExtentReportsConfigurator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyy");
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("MM-dd-yyyyТHH.mm.ss");
    private static final String PATH_FOR_REPORT_FOLDER = "%s%s%s";
    private static final String REPORT_NAME_TEMPLATE = "%s%s_%s.html";
    private static final String TEMPLATE_FOR_REPORT_SAVING_PATH = "%s%s%s%s";

    public void configure(final ExtentReports extentReports) {
        com.knubisoft.testlum.testing.model.global_config.ExtentReports extentReportsConfig =
                ConfigProviderImpl.GlobalTestConfigurationProvider.provide().getReport().getExtentReports();
        String projectName = extentReportsConfig.getProjectName();
        HtmlReportGenerator htmlReportGeneratorSettings = extentReportsConfig.getHtmlReportGenerator();
        KlovServerReportGenerator klovServerGeneratorSettings = extentReportsConfig.getKlovServerReportGenerator();
        if (htmlReportGeneratorSettings.isEnabled()) {
            attachSparkReporter(extentReports, projectName);
        }
        if (Objects.nonNull(klovServerGeneratorSettings) && klovServerGeneratorSettings.isEnabled()) {
            attachKlovServerReporter(extentReports, klovServerGeneratorSettings, projectName);
        }
    }

    private void attachSparkReporter(final ExtentReports extentReports, final String projectName) {
        LocalDateTime dateTime = LocalDateTime.now();
        String pathForReportFolder = format(PATH_FOR_REPORT_FOLDER,
                TestResourceSettings.getInstance().getTestResourcesFolder().getAbsolutePath(),
                separator, TestResourceSettings.REPORT_FOLDER);
        String reportName = format(REPORT_NAME_TEMPLATE, separator, projectName, dateTime.format(DATE_TIME_FORMATTER));
        String formattedPathForReportSaving = format(TEMPLATE_FOR_REPORT_SAVING_PATH,
                pathForReportFolder, separator, dateTime.format(DATE_FORMATTER), reportName);
        try {
            ExtentSparkReporter extentSparkReporter = new ExtentSparkReporter(formattedPathForReportSaving);
            extentReports.attachReporter(extentSparkReporter);
        } catch (Exception e) {
            log.error("Unable to create report file by path: {}", formattedPathForReportSaving);
        }
    }

    private void attachKlovServerReporter(final ExtentReports extentReports,
                                          final KlovServerReportGenerator klovServerGeneratorSettings,
                                          final String projectName) {
        Mongodb mongodbSettings = klovServerGeneratorSettings.getMongoDB();
        String klovServerURL = klovServerGeneratorSettings.getKlovServer().getUrl();
        ExtentKlovReporter extentKlovReporter = new ExtentKlovReporter(projectName);
        extentKlovReporter.initMongoDbConnection(mongodbSettings.getHost(), mongodbSettings.getPort().intValue());
        extentKlovReporter.initKlovServerConnection(klovServerURL);
        extentReports.attachReporter(extentKlovReporter);
    }
}
