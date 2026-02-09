package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.scenario.ScenarioArguments;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AbstractUiCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAlert;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertChecked;
import com.knubisoft.testlum.testing.model.scenario.AssertPresent;
import com.knubisoft.testlum.testing.model.scenario.AssertTitle;
import com.knubisoft.testlum.testing.model.scenario.CommandWithLocator;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import com.knubisoft.testlum.testing.model.scenario.Exclude;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.MobileImage;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import com.knubisoft.testlum.testing.model.scenario.Overview;
import com.knubisoft.testlum.testing.model.scenario.OverviewPart;
import com.knubisoft.testlum.testing.model.scenario.Part;
import com.knubisoft.testlum.testing.model.scenario.Picture;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.Ui;
import com.knubisoft.testlum.testing.model.scenario.WebFullScreen;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@UtilityClass
@Slf4j
public class LogUtil {

    /* execution log */
    public void logScenarioDetails(final ScenarioArguments scenarioArguments, final int scenarioId) {
        log.info(DelimiterConstant.EMPTY);
        log.info(LogMessage.SCENARIO_NUMBER_AND_PATH_LOG, scenarioId, scenarioArguments.getFile().getAbsolutePath());
        Overview overview = scenarioArguments.getScenario().getOverview();
        logOverview(overview);
        if (scenarioArguments.isContainsUiSteps()) {
            logUiInfo(scenarioArguments.getScenario().getSettings().getVariations(),
                    scenarioArguments.getEnvironment(),
                    scenarioArguments.getBrowser(),
                    scenarioArguments.getMobilebrowserDevice(),
                    scenarioArguments.getNativeDevice());
        }
    }

    private void logOverview(final Overview overview) {
        logOverviewPartInfo(OverviewPart.NAME, overview.getName());
        logOverviewPartInfo(OverviewPart.DESCRIPTION, overview.getDescription());
        logOverviewPartInfo(OverviewPart.JIRA, overview.getJira());
        logOverviewPartInfo(OverviewPart.DEVELOPER, overview.getDeveloper());
        logOverviewPartInfo(OverviewPart.LINK, overview.getLink());
    }

    private void logOverviewPartInfo(final OverviewPart overviewPart, final String data) {
        if (StringUtils.isNotBlank(data)) {
            log.info(LogMessage.OVERVIEW_INFO_LOG, overviewPart.value(), data);
        }
    }

    private void logUiInfo(final String variation,
                           final String environment,
                           final String browserAlias,
                           final String mobilebrowserAlias,
                           final String nativeDeviceAlias) {
        if (StringUtils.isNotBlank(variation)) {
            log.info(LogMessage.VARIATION_LOG, variation);
        }
        BrowserUtil.getBrowserBy(environment, browserAlias).ifPresent(abstractBrowser ->
                log.info(LogMessage.BROWSER_NAME_LOG, BrowserUtil.getBrowserInfo(abstractBrowser)));

        MobileUtil.getMobileBrowserDeviceBy(environment, mobilebrowserAlias).ifPresent(mobilebrowserDevice ->
                log.info(LogMessage.MOBILEBROWSER_LOG, MobileUtil.getMobileBrowserDeviceInfo(mobilebrowserDevice)));

        MobileUtil.getNativeDeviceBy(environment, nativeDeviceAlias).ifPresent(nativeDevice ->
                log.info(LogMessage.NATIVE_LOG, MobileUtil.getNativeDeviceInfo(nativeDevice)));
    }

    public void logTestExecutionSummary(final TestExecutionSummary testExecutionSummary) {
        if (testExecutionSummary.getTestsFoundCount() == 0 && !testExecutionSummary.getFailures().isEmpty()) {
            testExecutionSummary.getFailures().forEach(e -> log.error(LogMessage.TESTS_RUN_FAILED, e.getException()));
        } else {
            logTestsStatistics(testExecutionSummary);
        }
    }

    private void logTestsStatistics(final TestExecutionSummary testExecutionSummary) {
        log.info(LogMessage.TEST_EXECUTION_SUMMARY_TEMPLATE,
                testExecutionSummary.getTestsFoundCount(),
                testExecutionSummary.getTestsSkippedCount(),
                testExecutionSummary.getTestsStartedCount(),
                testExecutionSummary.getTestsAbortedCount(),
                testExecutionSummary.getTestsSucceededCount(),
                testExecutionSummary.getTestsFailedCount());
        testExecutionSummary.getFailures().forEach(e -> log.error(
                String.format(LogMessage.FAILED_SCENARIOS_NAME_TEMPLATE, e.getTestIdentifier().getDisplayName()),
                e.getException()));
    }

    public void logNonParsedScenarioInfo(final String path, final String exception) {
        log.error(LogMessage.INVALID_SCENARIO_LOG, path, exception);
    }

    /* general log */

    public void logCondition(final String name, final boolean condition) {
        if (!condition) {
            log.info(LogMessage.COMMAND_SKIPPED_ON_CONDITION_LOG);
        }
        log.info(LogMessage.CONDITION_LOG, name, condition);
    }

    public void logConditionInfo(final String name,
                                 final String expression,
                                 final boolean value) {
        log.info(LogMessage.NAME_LOG, name);
        log.info(LogMessage.EXPRESSION_LOG, expression);
        log.info(LogMessage.VALUE_LOG, value);
    }

    public void logExecutionTime(final long time, final AbstractCommand command) {
        if (Ui.class.isAssignableFrom(command.getClass())) {
            log.info(LogMessage.UI_EXECUTION_TIME_LOG, time);
        } else {
            log.info(LogMessage.EXECUTION_TIME_LOG, time);
        }
    }

    public void logException(final Exception ex) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.error(LogMessage.EXCEPTION_LOG,
                    ex.getMessage().replaceAll(LogMessage.REGEX_NEW_LINE, LogMessage.NEW_LOG_LINE));
        } else {
            log.error(LogMessage.EXCEPTION_LOG, ex.toString());
        }
    }

    /* integrations log */
    public void logAllQueries(final List<String> queries, final String alias) {
        log.info(LogMessage.ALIAS_LOG, alias);
        queries.forEach(query -> log.info(LogMessage.QUERY,
                query.replaceAll(DelimiterConstant.REGEX_MANY_SPACES, DelimiterConstant.SPACE)));
    }

    public void logAllQueries(final String dbType, final List<String> queries, final String alias) {
        log.info(LogMessage.DB_TYPE_LOG, dbType);
        logAllQueries(queries, alias);
    }

    public void logSqlException(final Exception ex, final String query) {
        if (StringUtils.isNotBlank(ex.getMessage())) {
            log.error(LogMessage.ERROR_SQL_QUERY,
                    ex.getMessage().replaceAll(LogMessage.REGEX_NEW_LINE, LogMessage.NEW_LOG_LINE),
                    SqlUtil.getBrokenQuery(ex, query).replaceAll(LogMessage.REGEX_NEW_LINE, LogMessage.NEW_LOG_LINE));
        } else {
            log.error(LogMessage.ERROR_SQL_QUERY,
                    ex.toString().replaceAll(LogMessage.REGEX_NEW_LINE, LogMessage.NEW_LOG_LINE));
        }
    }

    public void logVarInfo(final String name, final String value) {
        log.info(LogMessage.NAME_LOG, name);
        log.info(LogMessage.VALUE_LOG, StringPrettifier.cut(value));
    }

    /* ui log */
    public void logUICommand(final long position, final AbstractCommand action) {
        if (position != 0) {
            log.info(LogMessage.UI_COMMAND_LOG, position, action.getClass().getSimpleName());
        } else {
            log.info(LogMessage.UI_COMMAND_LOG_WITHOUT_POSITION, action.getClass().getSimpleName());
        }
        if (StringUtils.isNotBlank(action.getComment())) {
            log.info(LogMessage.COMMENT_LOG, action.getComment());
        }
        if (action instanceof CommandWithLocator) {
            log.info(LogMessage.LOCATOR_LOG, ((CommandWithLocator) action).getLocator());
        }
    }
    public void logUiAttributes(final boolean isClearCookies, final String storageKey) {
        log.info(LogMessage.CLEAR_COOKIES_AFTER, isClearCookies);
        if (StringUtils.isNotBlank(storageKey)) {
            log.info(LogMessage.LOCAL_STORAGE_KEY, storageKey);
        }
    }

    public void startUiCommandsInFrame() {
        log.info(LogMessage.START_UI_COMMANDS_IN_FRAME);
    }

    public void endUiCommandsInFrame() {
        log.info(LogMessage.END_UI_COMMANDS_IN_FRAME);
    }

    public void startUiCommandsInWebView() {
        log.info(LogMessage.START_UI_COMMANDS_IN_WEBVIEW);
    }

    public void endUiCommandsInWebView() {
        log.info(LogMessage.END_UI_COMMANDS_IN_WEBVIEW);
    }


    public void logImageComparisonInfo(final Image image) {
        log.info(LogMessage.IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(LogMessage.HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (Objects.nonNull(image.getPicture())) {
            logCompareWithElementInfo(image.getPicture());
        } else if (Objects.nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (Objects.nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    public void logImageComparisonInfo(final MobileImage image) {
        log.info(LogMessage.IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(LogMessage.HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (Objects.nonNull(image.getPicture())) {
            logCompareWithElementInfo(image.getPicture());
        } else if (Objects.nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (Objects.nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    public void logImageComparisonInfo(final NativeImage image) {
        log.info(LogMessage.IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(LogMessage.HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (Objects.nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (Objects.nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    private void logCompareWithElementInfo(final Picture element) {
        log.info(LogMessage.IMAGE_COMPARISON_TYPE_LOG, LogMessage.EXTRACT_THEN_COMPARE);
        log.info(LogMessage.LOCATOR_LOG, element.getLocator());
        log.info(LogMessage.IMAGE_SOURCE_ATT_LOG, element.getAttribute());
    }

    private void logCompareWithFullscreen(final WebFullScreen fullScreen) {
        log.info(LogMessage.IMAGE_COMPARISON_TYPE_LOG, LogMessage.TAKE_SCREENSHOT_THEN_COMPARE);
        if (Objects.nonNull(fullScreen.getPercentage())) {
            log.info(LogMessage.IMAGE_MATCH_PERCENTAGE_LOG, fullScreen.getPercentage());
        }
        if (!fullScreen.getExclude().isEmpty()) {
            log.info(LogMessage.IMAGE_EXCLUDED_ELEMENT_LOG, StringUtils.join(fullScreen.getExclude().stream()
                    .map(Exclude::getLocator)
                    .collect(Collectors.joining(DelimiterConstant.COMMA + DelimiterConstant.SPACE))));
        }
    }

    private void logCompareWithFullscreen(final FullScreen fullScreen) {
        log.info(LogMessage.IMAGE_COMPARISON_TYPE_LOG, LogMessage.TAKE_SCREENSHOT_THEN_COMPARE);
        if (Objects.nonNull(fullScreen.getPercentage())) {
            log.info(LogMessage.IMAGE_MATCH_PERCENTAGE_LOG, fullScreen.getPercentage());
        }
    }

    private void logCompareWithPart(final Part part) {
        log.info(LogMessage.IMAGE_COMPARISON_TYPE_LOG, LogMessage.GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE);
        log.info(LogMessage.LOCATOR_LOG, part.getLocator());
        if (Objects.nonNull(part.getPercentage())) {
            log.info(LogMessage.IMAGE_MATCH_PERCENTAGE_LOG, part.getPercentage());
        }
    }

    public void logScrollInfo(final Scroll scroll) {
        log.info(LogMessage.SCROLL_DIRECTION_LOG, scroll.getDirection());
        log.info(LogMessage.SCROLL_BY_LOG, scroll.getMeasure());
        log.info(LogMessage.VALUE_LOG, scroll.getValue());
        log.info(LogMessage.SCROLL_TYPE, scroll.getType());
        if (ScrollType.INNER == scroll.getType()) {
            log.info(LogMessage.SCROLL_LOCATOR, scroll.getLocator());
            log.info(LogMessage.LOCATOR_STRATEGY, scroll.getLocatorStrategy());
        }
    }

    public void logHover(final Hover hover) {
        if (hover.isMoveToEmptySpace()) {
            log.info(LogMessage.MOVE_TO_EMPTY_SPACE, hover.isMoveToEmptySpace());
        }
    }

    public void logHotKeyInfo(final AbstractUiCommand command, final int position) {
        log.info(LogMessage.COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(LogMessage.COMMENT_LOG, command.getComment());
    }

    public void logSingleKeyCommandTimes(final int times) {
        if (times > 1) {
            log.info(LogMessage.HOTKEY_COMMAND_TIMES, times);
        }
    }

    public void logCloseOrSwitchTabCommand(final String command, final Integer tabNumber) {
        log.info(LogMessage.TAB_COMMAND, command);
        log.info(LogMessage.TAB_INDEX, Objects.nonNull(tabNumber) ? tabNumber : ResultUtil.LAST_TAB);
    }

    public void logOpenTabCommand(final String url) {
        log.info(LogMessage.TAB_COMMAND, ResultUtil.OPEN_TAB);
        log.info(LogMessage.TAB_URL, StringUtils.isNotBlank(url) ? url : ResultUtil.WITHOUT_URL);
    }

    public void logAssertCommand(final AbstractCommand command, final int position) {
        log.info(LogMessage.COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(LogMessage.COMMENT_LOG, command.getComment());
    }

    public void logAssertAttributeInfo(final AssertAttribute attribute) {
        log.info(LogMessage.NEGATIVE_LOG, attribute.isNegative());
        log.info(LogMessage.LOCATOR_LOG, attribute.getLocator());
        log.info(LogMessage.ATTRIBUTE_LOG, attribute.getName());
        log.info(LogMessage.CONTENT_LOG, StringPrettifier.cut(attribute.getContent()));
    }

    public void logAssertTitleCommand(final AssertTitle title) {
        log.info(LogMessage.NEGATIVE_LOG, title.isNegative());
        log.info(LogMessage.CONTENT_LOG, title.getContent());
    }

    public void logAssertAlertCommand(final AssertAlert alert) {
        log.info(LogMessage.NEGATIVE_LOG, alert.isNegative());
        log.info(LogMessage.CONTENT_LOG, alert.getText());
    }

    public void logDragAndDropInfo(final DragAndDrop dragAndDrop) {
        if (StringUtils.isNotBlank(dragAndDrop.getFileName())) {
            log.info(LogMessage.DRAGGING_FILE_PATH, dragAndDrop.getFileName());
        } else if (StringUtils.isNotBlank(dragAndDrop.getFromLocator())) {
            log.info(LogMessage.DRAGGING_FROM, dragAndDrop.getFromLocator());
        }
        log.info(LogMessage.DROPPING_TO, dragAndDrop.getToLocator());
    }

    public void logDragAndDropNativeInfo(final DragAndDropNative dragAndDropNative) {
        log.info(LogMessage.DRAGGING_FROM, dragAndDropNative.getFromLocator());
        log.info(LogMessage.DROPPING_TO, dragAndDropNative.getToLocator());
    }

    public void logSwipeNativeInfo(final SwipeNative swipeNative) {
        if (swipeNative.getElement() != null) {
            log.info(LogMessage.SWIPE_TYPE, "ELEMENT");
            log.info(LogMessage.SWIPE_QUANTITY, swipeNative.getElement().getQuantity());
            log.info(LogMessage.SWIPE_DIRECTION, swipeNative.getElement().getDirection());
            log.info(LogMessage.SWIPE_VALUE, swipeNative.getElement().getPercent());
            log.info(LogMessage.LOCATOR_LOG, swipeNative.getElement().getLocator());
        } else {
            log.info(LogMessage.SWIPE_TYPE, "PAGE");
            log.info(LogMessage.SWIPE_QUANTITY, swipeNative.getPage().getQuantity());
            log.info(LogMessage.SWIPE_DIRECTION, swipeNative.getPage().getDirection());
            log.info(LogMessage.SWIPE_VALUE, swipeNative.getPage().getPercent());
        }
    }

    public void logAssertPresent(final AssertPresent command) {
        log.info(LogMessage.LOCATOR_LOG, command.getLocator());
        log.info(LogMessage.NEGATIVE_LOG, command.isNegative());
    }

    public void logAssertChecked(final AssertChecked command) {
        log.info(LogMessage.LOCATOR_LOG, command.getLocator());
        log.info(LogMessage.NEGATIVE_LOG, command.isNegative());
    }

    public static void logScenarioWithoutTags(final String scenarioPath) {
        log.warn(LogMessage.SCENARIO_WITH_EMPTY_TAG_LOG, scenarioPath);
    }
}
