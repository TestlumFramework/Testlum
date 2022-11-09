package com.knubisoft.cott.testing.framework.report.extentreports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.reporter.ExtentKlovReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.cott.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.cott.testing.model.global_config.Mongodb;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.Objects;

import static java.lang.String.format;

@UtilityClass
public class ExtentReportsConfigurator {

    private static final String TEMPLATE_FOR_REPORT_SAVING_PATH = "%s/%s/%s_%s.html";

    public void configure(final ExtentReports extentReports) {
        com.knubisoft.cott.testing.model.global_config.ExtentReports extentReportsConfig =
                GlobalTestConfigurationProvider.provide().getReport().getExtentReports();
        String projectName = extentReportsConfig.getProjectName();
        HtmlReportGenerator htmlReportGeneratorSettings = extentReportsConfig.getHtmlReportGenerator();
        KlovServerReportGenerator klovServerGeneratorSettings = extentReportsConfig.getKlovServerReportGenerator();
        if (htmlReportGeneratorSettings.isEnable()) {
            attachSparkReporter(extentReports, projectName);
        }
        if (Objects.nonNull(klovServerGeneratorSettings) && klovServerGeneratorSettings.isEnable()) {
            attachKlovServerReporter(extentReports, klovServerGeneratorSettings, projectName);
        }
    }

    private void attachSparkReporter(final ExtentReports extentReports, final String projectName) {
        LocalDateTime dateTime = LocalDateTime.now();
        String pathForReportSaving = TestResourceSettings.getInstance().getTestResourcesFolder().getAbsolutePath()
                + TestResourceSettings.REPORT_FOLDER;
        String formattedPathForReportSaving = format(TEMPLATE_FOR_REPORT_SAVING_PATH, pathForReportSaving,
                        dateTime.format(TestResourceSettings.DATE_FORMATER),
                        projectName, dateTime.format(TestResourceSettings.DATE_TIME_FORMATTER));
        ExtentSparkReporter extentSparkReporter = new ExtentSparkReporter(formattedPathForReportSaving);
        extentReports.attachReporter(extentSparkReporter);
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
