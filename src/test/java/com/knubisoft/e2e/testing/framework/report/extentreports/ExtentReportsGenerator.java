package com.knubisoft.e2e.testing.framework.report.extentreports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.aventstack.extentreports.reporter.ExtentKlovReporter;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.e2e.testing.framework.report.ReportGenerator;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.report.ScenarioResult;
import com.knubisoft.e2e.testing.framework.report.extentreports.model.ResultForComparison;
import com.knubisoft.e2e.testing.model.global_config.HtmlReportGenerator;
import com.knubisoft.e2e.testing.model.global_config.KlovServerReportGenerator;
import com.knubisoft.e2e.testing.model.global_config.Mongodb;
import com.knubisoft.e2e.testing.model.scenario.Overview;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.lang.String.format;

public class ExtentReportsGenerator implements ReportGenerator {

    //because of quality-checking
    private static final int ZERO = 0;
    private static final int ONE = 1;
    private static final int TWO = 2;
    private static final int THREE = 3;
    private static final int FOUR = 4;

    private static final String SCENARIO_NAME = "Scenario name:";
    private static final String PATH_TO_SCENARIO = "Path to scenario:";
    private static final String DEVELOPER = "Developer:";
    private static final String JIRA = "Jira:";
    private static final String LINK_TEMPLATE = "<a href='%s'>%s<a/>";
    private static final String PREFORMATTED_TEXT_TEMPLATE = "<pre>%s</pre>";
    private static final String PREFORMATTED_CODE_TEXT_TEMPLATE = "<pre><code>%s</code></pre>";
    private static final String BOLD_TEXT_TEMPLATE = "<b>%s</b>";
    private static final String UNSORTED_LIST_TEMPLATE = "<ul>%s</ul>";
    private static final String UNSORTED_LIST_ITEM_TEMPLATE = "<li>%s</li>";
    private static final String SCENARIO_NAME_TEMPLATE = "Scenario #%d - %s";
    private static final String SCENARIO_STEP_NAME_TEMPLATE = "Scenario step #%d - %s";
    private static final String STEP_SUCCESS = "Scenario step executed successfully";
    private static final String SCREENSHOT = "Step screenshot";
    private static final String SCENARIO_SUCCESS = "Test scenario passed successfully";
    private static final String SCENARIO_FAILED = "Test scenario failed";
    private static final String SCENARIO_EXECUTION_TIME_TEMPLATE = "Test scenario execution time: %dms";
    private static final String STEP_EXECUTION_TIME_TEMPLATE = "Step execution time: %dms";
    private static final String PATH_FOR_REPORT_SAVING_TEMPLATE = "%s%s_%s";

    @Override
    public void generateReport(final GlobalScenarioStatCollector globalScenarioStatCollector) {
        ExtentReports extentReports = new ExtentReports();
        attachReporters(extentReports);
        globalScenarioStatCollector.getResults()
                .forEach(scenarioExecutionResult -> addScenarioExecutionResult(extentReports, scenarioExecutionResult));
        extentReports.flush();
    }

    private void attachReporters(final ExtentReports extentReports) {
        com.knubisoft.e2e.testing.model.global_config.ExtentReports extentReportsConfig =
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
        String pathForSaving =
                TestResourceSettings.getInstance().getTestResourcesFolder().getAbsolutePath() + "/report/";
        String formattedPath = format(PATH_FOR_REPORT_SAVING_TEMPLATE, pathForSaving, projectName, LocalDateTime.now());
        ExtentSparkReporter extentSparkReporter = new ExtentSparkReporter(formattedPath);
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

    private void addScenarioExecutionResult(final ExtentReports extentReports, final ScenarioResult scenarioResult) {
        ExtentTest extentTest = extentReports
                .createTest(format(SCENARIO_NAME_TEMPLATE, scenarioResult.getId(), scenarioResult.getName()));
        extentTest.assignCategory(scenarioResult.getTags().getTag().toArray(new String[0]));
        addOverviewInfo(extentTest, scenarioResult.getOverview(), scenarioResult.getPath());
        setExecutionResult(extentTest, scenarioResult);
        addScenarioSteps(extentTest, scenarioResult.getCommands());
    }

    private void setExecutionResult(final ExtentTest extentTest, final ScenarioResult scenarioResult) {
        if (scenarioResult.isSuccess()) {
            extentTest.pass(MarkupHelper.createLabel(SCENARIO_SUCCESS, ExtentColor.GREEN));
        } else {
            extentTest.fail(MarkupHelper.createLabel(SCENARIO_FAILED, ExtentColor.RED));
        }
        extentTest.info(format(SCENARIO_EXECUTION_TIME_TEMPLATE, scenarioResult.getExecutionTime()));
    }

    private void addOverviewInfo(final ExtentTest extentTest, final Overview overview, final String filePath) {
        String developerName = overview.getDeveloper();
        addDeveloper(extentTest, developerName);
        extentTest.info(MarkupHelper.createLabel(overview.getDescription(), ExtentColor.ORANGE));
        extentTest.info(MarkupHelper.createTable(createTableWithOverviewInfo(overview, filePath, developerName)));
        addLinks(extentTest, overview.getLink());
    }

    private void addDeveloper(final ExtentTest extentTest, final String developerName) {
        if (StringUtils.isNotEmpty(developerName)) {
            extentTest.assignAuthor(developerName);
        }
    }

    private void addLinks(final ExtentTest extentTest, final List<String> linksForReport) {
        if (!linksForReport.isEmpty()) {
            List<String> clickableLinks = linksForReport.stream()
                    .map(link -> format(LINK_TEMPLATE, link, link)).collect(Collectors.toList());
            extentTest.info(MarkupHelper.createUnorderedList(clickableLinks));
        }
    }

    private String[][] createTableWithOverviewInfo(final Overview overview,
                                                   final String filePath,
                                                   final String developerName) {
        String[][] overviewTable = new String[FOUR][TWO];
        overviewTable[ZERO][ZERO] = format(BOLD_TEXT_TEMPLATE, SCENARIO_NAME);
        overviewTable[ZERO][ONE] = overview.getName();
        overviewTable[ONE][ZERO] = format(BOLD_TEXT_TEMPLATE, PATH_TO_SCENARIO);
        overviewTable[ONE][ONE] = filePath;
        overviewTable[TWO][ZERO] = format(BOLD_TEXT_TEMPLATE, DEVELOPER);
        overviewTable[TWO][ONE] = StringUtils.isNotEmpty(developerName) ? developerName : StringUtils.EMPTY;
        overviewTable[THREE][ZERO] = format(BOLD_TEXT_TEMPLATE, JIRA);
        String jira = overview.getJira();
        overviewTable[THREE][ONE] = StringUtils.isNotEmpty(jira)
                ? format(LINK_TEMPLATE, jira, jira) : StringUtils.EMPTY;
        return overviewTable;
    }

    private void addScenarioSteps(final ExtentTest extentTest, final List<CommandResult> steps) {
        steps.forEach(step -> createStepInfo(extentTest, step));
    }

    private void createStepInfo(final ExtentTest extentTest, final CommandResult stepExecutionInfo) {
        String stepCommandKey = format(BOLD_TEXT_TEMPLATE, stepExecutionInfo.getCommandKey());
        String stepName = format(SCENARIO_STEP_NAME_TEMPLATE, stepExecutionInfo.getId(), stepCommandKey);
        ExtentTest step = extentTest.createNode(stepName);
        step.info(MarkupHelper.createLabel(stepExecutionInfo.getComment(), ExtentColor.BLUE));
        addScreenshotIfExists(step, stepExecutionInfo.getBase64Screenshot());
        addMetaData(step, stepExecutionInfo.getMetadata());
        addExpectedAndActual(step, stepExecutionInfo);
        setStepExecutionResult(step, stepExecutionInfo);
        if (CollectionUtils.isNotEmpty(stepExecutionInfo.getSubCommandsResult())) {
            addScenarioSteps(step, stepExecutionInfo.getSubCommandsResult());
        }
    }

    @SneakyThrows
    private void addScreenshotIfExists(final ExtentTest extentTest, final String screenshot) {
        if (StringUtils.isNotEmpty(screenshot)) {
            extentTest.info(MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot,
                    format(BOLD_TEXT_TEMPLATE, SCREENSHOT)).build());
        }
    }

    private void addExpectedAndActual(final ExtentTest extentTest, final CommandResult stepExecutionInfo) {
        String expected = stepExecutionInfo.getExpected();
        String actual = stepExecutionInfo.getActual();
        if (StringUtils.isNotEmpty(expected) && StringUtils.isNotEmpty(actual)) {
            ResultForComparison resultForComparison =
                    new ResultForComparison(format(PREFORMATTED_CODE_TEXT_TEMPLATE, expected),
                    format(PREFORMATTED_CODE_TEXT_TEMPLATE, actual));
            extentTest.info(MarkupHelper.toTable(resultForComparison));
        }
    }

    private void addMetaData(final ExtentTest extentTest, final LinkedHashMap<String, Object> metaData) {
        if (!metaData.isEmpty()) {
            String[][] metaDataTable = metaData.entrySet()
                    .stream()
                    .map(e -> new String[]{format(BOLD_TEXT_TEMPLATE, e.getKey() + DelimiterConstant.COLON),
                            getValueAsStringFromMetaData(e.getValue())})
                    .toArray(String[][]::new);
            extentTest.info(MarkupHelper.createTable(metaDataTable));
        }
    }

    private String getValueAsStringFromMetaData(final Object value) {
        if (value instanceof List) {
            StringBuilder valueAsTable = new StringBuilder();
            ((List<?>) value).forEach(v -> valueAsTable.append(format(UNSORTED_LIST_ITEM_TEMPLATE, v)));
            return format(UNSORTED_LIST_TEMPLATE, valueAsTable);
        }
        return format(PREFORMATTED_TEXT_TEMPLATE, value);
    }

    private void setStepExecutionResult(final ExtentTest extentTest, final CommandResult stepExecutionInfo) {
        if (stepExecutionInfo.isSuccess()) {
            extentTest.pass(MarkupHelper.createLabel(STEP_SUCCESS, ExtentColor.GREEN));
        } else {
            extentTest.fail(stepExecutionInfo.getException());
        }
        extentTest.info(format(STEP_EXECUTION_TIME_TEMPLATE, stepExecutionInfo.getExecutionTime()));
    }
}
