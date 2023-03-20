package com.knubisoft.cott.testing.framework.report.extentreports;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.MediaEntityBuilder;
import com.aventstack.extentreports.markuputils.ExtentColor;
import com.aventstack.extentreports.markuputils.MarkupHelper;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.report.GlobalScenarioStatCollector;
import com.knubisoft.cott.testing.framework.report.ReportGenerator;
import com.knubisoft.cott.testing.framework.report.ScenarioResult;
import com.knubisoft.cott.testing.framework.report.extentreports.model.ResultForComparison;
import com.knubisoft.cott.testing.framework.util.BrowserUtil;
import com.knubisoft.cott.testing.framework.util.MobileUtil;
import com.knubisoft.cott.testing.model.global_config.AbstractBrowser;
import com.knubisoft.cott.testing.model.global_config.AbstractCapabilities;
import com.knubisoft.cott.testing.model.global_config.MobilebrowserDevice;
import com.knubisoft.cott.testing.model.global_config.NativeDevice;
import com.knubisoft.cott.testing.model.scenario.Overview;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Comparator;
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
    private static final String BROWSER = "Browser:";
    private static final String BROWSER_TYPE = "Browser type:";
    private static final String BROWSER_VERSION = "Browser version:";
    private static final String NATIVE_DEVICE = "Native device:";
    private static final String MOBILEBROWSER_DEVICE = "Mobilebrowser device:";
    private static final String DEVICE_PLATFORM = "Device platform:";
    private static final String DEVICE_PLATFORM_VERSION = "Device platform version:";
    private static final String DEVICE_UDID = "Device udid:";
    private static final String APP_PACKAGE = "App package:";
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

    @Override
    public void generateReport(final GlobalScenarioStatCollector globalScenarioStatCollector) {
        ExtentReports extentReports = new ExtentReports();
        ExtentReportsConfigurator.configure(extentReports);
        globalScenarioStatCollector
                .getResults().stream()
                .sorted(Comparator.comparing(ScenarioResult::getId))
                .forEach(scenarioExecutionResult -> addScenarioExecutionResult(extentReports, scenarioExecutionResult));
        extentReports.flush();
    }

    private void addScenarioExecutionResult(final ExtentReports extentReports, final ScenarioResult scenarioResult) {
        ExtentTest extentTest = extentReports
                .createTest(format(SCENARIO_NAME_TEMPLATE, scenarioResult.getId(), scenarioResult.getName()));
        extentTest.assignCategory(scenarioResult.getTags().getTag().toArray(new String[0]));
        addOverviewInfo(extentTest, scenarioResult.getOverview(), scenarioResult.getPath());
        addBrowserInfo(extentTest, scenarioResult);
        addMobilebrowserDeviceInfo(extentTest, scenarioResult);
        addNativeDeviceInfo(extentTest, scenarioResult);
        setExecutionResult(extentTest, scenarioResult);
        addScenarioSteps(extentTest, scenarioResult.getCommands());
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

    private String[][] createTableWithBrowserInfo(final AbstractBrowser browser) {
        String[][] browserInfoTable = new String[THREE][TWO];
        browserInfoTable[ZERO][ZERO] = format(BOLD_TEXT_TEMPLATE, BROWSER);
        browserInfoTable[ZERO][ONE] = browser.getClass().getSimpleName();
        browserInfoTable[ONE][ZERO] = format(BOLD_TEXT_TEMPLATE, BROWSER_TYPE);
        BrowserUtil.BrowserType browserType = BrowserUtil.getBrowserType(browser);
        browserInfoTable[ONE][ONE] = browserType.getTypeName();
        browserInfoTable[TWO][ZERO] = format(BOLD_TEXT_TEMPLATE, BROWSER_VERSION);
        browserInfoTable[TWO][ONE] = BrowserUtil.getBrowserVersion(browser, browserType);
        return browserInfoTable;
    }

    private String[][] createTableWithMobilebrowserDeviceInfo(final MobilebrowserDevice mobilebrowserDevice) {
        AbstractCapabilities capabilities = Objects.nonNull(mobilebrowserDevice.getAppiumCapabilities())
                ? mobilebrowserDevice.getAppiumCapabilities()
                : mobilebrowserDevice.getBrowserStackCapabilities();
        String[][] browserInfoTable = new String[THREE][TWO];
        browserInfoTable[ZERO][ZERO] = format(BOLD_TEXT_TEMPLATE, MOBILEBROWSER_DEVICE);
        browserInfoTable[ZERO][ONE] = capabilities.getDeviceName();
        browserInfoTable[ONE][ZERO] = format(BOLD_TEXT_TEMPLATE, DEVICE_PLATFORM);
        browserInfoTable[ONE][ONE] = mobilebrowserDevice.getPlatformName().value();
        browserInfoTable[TWO][ZERO] = format(BOLD_TEXT_TEMPLATE, DEVICE_PLATFORM_VERSION);
        browserInfoTable[TWO][ONE] = capabilities.getPlatformVersion();
        return browserInfoTable;
    }

    private String[][] createTableWithNativeDeviceInfo(final NativeDevice nativeDevice) {
        AbstractCapabilities capabilities = Objects.nonNull(nativeDevice.getAppiumCapabilities())
                ? nativeDevice.getAppiumCapabilities()
                : nativeDevice.getBrowserStackCapabilities();
        String[][] browserInfoTable = new String[THREE][TWO];
        browserInfoTable[ZERO][ZERO] = format(BOLD_TEXT_TEMPLATE, NATIVE_DEVICE);
        browserInfoTable[ZERO][ONE] = capabilities.getDeviceName();
        browserInfoTable[ONE][ZERO] = format(BOLD_TEXT_TEMPLATE, DEVICE_PLATFORM);
        browserInfoTable[ONE][ONE] = nativeDevice.getPlatformName().value();
        browserInfoTable[TWO][ZERO] = format(BOLD_TEXT_TEMPLATE, DEVICE_PLATFORM_VERSION);
        browserInfoTable[TWO][ONE] = capabilities.getPlatformVersion();
        return browserInfoTable;
    }

    private void addBrowserInfo(final ExtentTest extentTest, final ScenarioResult scenarioResult) {
        if (StringUtils.isNotBlank(scenarioResult.getBrowser())) {
            BrowserUtil.getBrowserBy(scenarioResult.getEnvironment(), scenarioResult.getBrowser())
                    .ifPresent(browser -> extentTest.info(
                            MarkupHelper.createTable(createTableWithBrowserInfo(browser))));
        }
    }

    private void addMobilebrowserDeviceInfo(final ExtentTest extentTest, final ScenarioResult scenarioResult) {
        if (StringUtils.isNotBlank(scenarioResult.getMobilebrowserDevice())) {
            MobileUtil.getMobilebrowserDeviceBy(scenarioResult.getEnvironment(),
                            scenarioResult.getMobilebrowserDevice())
                    .ifPresent(mobilebrowserDevice -> extentTest.info(
                            MarkupHelper.createTable(createTableWithMobilebrowserDeviceInfo(mobilebrowserDevice))));
        }
    }

    private void addNativeDeviceInfo(final ExtentTest extentTest, final ScenarioResult scenarioResult) {
        if (StringUtils.isNotBlank(scenarioResult.getNativeDevice())) {
            MobileUtil.getNativeDeviceBy(scenarioResult.getEnvironment(), scenarioResult.getNativeDevice())
                    .ifPresent(nativeDevice -> extentTest.info(
                            MarkupHelper.createTable(createTableWithNativeDeviceInfo(nativeDevice))));
        }
    }

    private void setExecutionResult(final ExtentTest extentTest, final ScenarioResult scenarioResult) {
        if (scenarioResult.isSuccess()) {
            extentTest.pass(MarkupHelper.createLabel(SCENARIO_SUCCESS, ExtentColor.GREEN));
        } else {
            extentTest.fail(MarkupHelper.createLabel(SCENARIO_FAILED, ExtentColor.RED));
        }
        extentTest.info(format(SCENARIO_EXECUTION_TIME_TEMPLATE, scenarioResult.getExecutionTime()));
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

    private void addScreenshotIfExists(final ExtentTest extentTest, final String screenshot) {
        if (StringUtils.isNotEmpty(screenshot)) {
            extentTest.info(MediaEntityBuilder.createScreenCaptureFromBase64String(screenshot,
                    format(BOLD_TEXT_TEMPLATE, SCREENSHOT)).build());
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

    private void setStepExecutionResult(final ExtentTest extentTest, final CommandResult stepExecutionInfo) {
        if (stepExecutionInfo.isSuccess()) {
            extentTest.pass(MarkupHelper.createLabel(STEP_SUCCESS, ExtentColor.GREEN));
        } else {
            extentTest.fail(stepExecutionInfo.getException());
        }
        extentTest.info(format(STEP_EXECUTION_TIME_TEMPLATE, stepExecutionInfo.getExecutionTime()));
    }
}
