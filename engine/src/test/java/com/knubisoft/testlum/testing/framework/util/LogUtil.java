package com.knubisoft.testlum.testing.framework.util;

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
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.REGEX_MANY_SPACES;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.*;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.LAST_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.OPEN_TAB;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.WITHOUT_URL;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
@Slf4j
public class LogUtil {

    private static final int MAX_CONTENT_LENGTH = 25 * 1024;

    /* execution log */
    public void logScenarioDetails(final ScenarioArguments scenarioArguments, final int scenarioId) {
        log.info(EMPTY);
        log.info(SCENARIO_NUMBER_AND_PATH_LOG, scenarioId, scenarioArguments.getFile().getAbsolutePath());
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
        if (isNotBlank(data)) {
            log.info(LogMessage.OVERVIEW_INFO_LOG, overviewPart.value(), data);
        }
    }

    private void logUiInfo(final String variation,
                           final String environment,
                           final String browserAlias,
                           final String mobilebrowserAlias,
                           final String nativeDeviceAlias) {
        if (isNotBlank(variation)) {
            log.info(VARIATION_LOG, variation);
        }
        BrowserUtil.getBrowserBy(environment, browserAlias).ifPresent(abstractBrowser ->
                log.info(BROWSER_NAME_LOG, BrowserUtil.getBrowserInfo(abstractBrowser)));

        MobileUtil.getMobilebrowserDeviceBy(environment, mobilebrowserAlias).ifPresent(mobilebrowserDevice ->
                log.info(MOBILEBROWSER_LOG, MobileUtil.getMobilebrowserDeviceInfo(mobilebrowserDevice)));

        MobileUtil.getNativeDeviceBy(environment, nativeDeviceAlias).ifPresent(nativeDevice ->
                log.info(NATIVE_LOG, MobileUtil.getNativeDeviceInfo(nativeDevice)));
    }

    public void logTestExecutionSummary(final TestExecutionSummary testExecutionSummary) {
        if (testExecutionSummary.getTestsFoundCount() == 0 && !testExecutionSummary.getFailures().isEmpty()) {
            testExecutionSummary.getFailures().forEach(e -> log.error(TESTS_RUN_FAILED, e.getException()));
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
                format(LogMessage.FAILED_SCENARIOS_NAME_TEMPLATE, e.getTestIdentifier().getDisplayName()),
                e.getException()));
    }

    public void logNonParsedScenarioInfo(final String path, final String exception) {
        log.error(INVALID_SCENARIO_LOG, path, exception);
    }

    /* general log */

    public void logAlias(final String alias) {
        log.info(ALIAS_LOG, alias);
    }

    public void logCondition(final String name, final boolean condition) {
        if (!condition) {
            log.info(COMMAND_SKIPPED_ON_CONDITION_LOG);
        }
        log.info(CONDITION_LOG, name, condition);
    }

    public void logConditionInfo(final String name,
                                 final String expression,
                                 final boolean value) {
        log.info(NAME_LOG, name);
        log.info(EXPRESSION_LOG, expression);
        log.info(VALUE_LOG, value);
    }

    public void logExecutionTime(final long time, final AbstractCommand command) {
        if (Ui.class.isAssignableFrom(command.getClass())) {
            log.info(UI_EXECUTION_TIME_LOG, time);
        } else {
            log.info(EXECUTION_TIME_LOG, time);
        }
    }

    public void logException(final Exception ex) {
        if (isNotBlank(ex.getMessage())) {
            log.error(EXCEPTION_LOG, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(EXCEPTION_LOG, ex.toString());
        }
    }

    public void logError(final Exception ex) {
        log.error(LogMessage.ERROR_LOG, ex);
    }

    /* integrations log */
    public void logAllQueries(final List<String> queries, final String alias) {
        log.info(ALIAS_LOG, alias);
        queries.forEach(query -> log.info(QUERY, query.replaceAll(REGEX_MANY_SPACES, SPACE)));
    }

    public void logAllQueries(final String dbType, final List<String> queries, final String alias) {
        log.info(DB_TYPE_LOG, dbType);
        logAllQueries(queries, alias);
    }

    public void logSqlException(final Exception ex, final String query) {
        if (isNotBlank(ex.getMessage())) {
            log.error(ERROR_SQL_QUERY, ex.getMessage().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE),
                    SqlUtil.getBrokenQuery(ex, query).replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        } else {
            log.error(ERROR_SQL_QUERY, ex.toString().replaceAll(REGEX_NEW_LINE, NEW_LOG_LINE));
        }
    }

    public void logHttpInfo(final String alias, final String method, final String endpoint) {
        log.info(ALIAS_LOG, alias);
        log.info(HTTP_METHOD_LOG, method);
        log.info(ENDPOINT_LOG, endpoint);
    }

    @SneakyThrows
    public void logBodyContent(final HttpEntity body) {
        if (nonNull(body) && body.getContentLength() < MAX_CONTENT_LENGTH) {
            logBody(IOUtils.toString(body.getContent(), StandardCharsets.UTF_8.name()));
        }
    }

    public void logBody(final String body) {
        if (isNotBlank(body)) {
            log.info(BODY_LOG,
                    StringPrettifier.asJsonResult(StringPrettifier.cut(body))
                            .replaceAll(REGEX_NEW_LINE, CONTENT_FORMAT));
        }
    }

    public void logVarInfo(final String name, final String value) {
        log.info(NAME_LOG, name);
        log.info(VALUE_LOG, StringPrettifier.cut(value));
    }

    /* ui log */
    public void logUICommand(final long position, final AbstractCommand action) {
        if (position != 0) {
            log.info(UI_COMMAND_LOG, position, action.getClass().getSimpleName());
        } else {
            log.info(UI_COMMAND_LOG_WITHOUT_POSITION, action.getClass().getSimpleName());
        }
        if (isNotBlank(action.getComment())) {
            log.info(COMMENT_LOG, action.getComment());
        }
        if (action instanceof CommandWithLocator) {
            log.info(LOCATOR_LOG, ((CommandWithLocator) action).getLocator());
        }
    }
    public void logUiAttributes(final boolean isClearCookies, final String storageKey) {
        log.info(CLEAR_COOKIES_AFTER, isClearCookies);
        if (isNotBlank(storageKey)) {
            log.info(LOCAL_STORAGE_KEY, storageKey);
        }
    }

    public void startUiCommandsInFrame() {
        log.info(START_UI_COMMANDS_IN_FRAME);
    }

    public void endUiCommandsInFrame() {
        log.info(END_UI_COMMANDS_IN_FRAME);
    }

    public void startUiCommandsInWebView() {
        log.info(START_UI_COMMANDS_IN_WEBVIEW);
    }

    public void endUiCommandsInWebView() {
        log.info(END_UI_COMMANDS_IN_WEBVIEW);
    }

    public void logSubCommand(final int position, final Object action) {
        log.info(COMMAND_LOG, position, action.getClass().getSimpleName());
    }

    public void logImageComparisonInfo(final Image image) {
        log.info(IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (nonNull(image.getPicture())) {
            logCompareWithElementInfo(image.getPicture());
        } else if (nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    public void logImageComparisonInfo(final MobileImage image) {
        log.info(IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (nonNull(image.getPicture())) {
            logCompareWithElementInfo(image.getPicture());
        } else if (nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    public void logImageComparisonInfo(final NativeImage image) {
        log.info(IMAGE_FOR_COMPARISON_LOG, image.getFile());
        log.info(HIGHLIGHT_DIFFERENCE_LOG, image.isHighlightDifference());
        if (nonNull(image.getFullScreen())) {
            logCompareWithFullscreen(image.getFullScreen());
        } else if (nonNull(image.getPart())) {
            logCompareWithPart(image.getPart());
        }
    }

    private void logCompareWithElementInfo(final Picture element) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, EXTRACT_THEN_COMPARE);
        log.info(LOCATOR_LOG, element.getLocator());
        log.info(IMAGE_SOURCE_ATT_LOG, element.getAttribute());
    }

    private void logCompareWithFullscreen(final WebFullScreen fullScreen) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, TAKE_SCREENSHOT_THEN_COMPARE);
        if (nonNull(fullScreen.getPercentage())) {
            log.info(IMAGE_MATCH_PERCENTAGE_LOG, fullScreen.getPercentage());
        }
        if (!fullScreen.getExclude().isEmpty()) {
            log.info(IMAGE_EXCLUDED_ELEMENT_LOG, StringUtils.join(fullScreen.getExclude().stream()
                    .map(ImageComparisonUtil::addExcludedMetaData)
                    .collect(Collectors.joining(COMMA + SPACE))));
        }
    }

    private void logCompareWithFullscreen(final FullScreen fullScreen) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, TAKE_SCREENSHOT_THEN_COMPARE);
        if (nonNull(fullScreen.getPercentage())) {
            log.info(IMAGE_MATCH_PERCENTAGE_LOG, fullScreen.getPercentage());
        }
    }

    private void logCompareWithPart(final Part part) {
        log.info(IMAGE_COMPARISON_TYPE_LOG, GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE);
        log.info(LOCATOR_LOG, part.getLocator());
        if (nonNull(part.getPercentage())) {
            log.info(IMAGE_MATCH_PERCENTAGE_LOG, part.getPercentage());
        }
    }

    public void logScrollInfo(final Scroll scroll) {
        log.info(SCROLL_DIRECTION_LOG, scroll.getDirection());
        log.info(SCROLL_BY_LOG, scroll.getMeasure());
        log.info(VALUE_LOG, scroll.getValue());
        log.info(SCROLL_TYPE, scroll.getType());
        if (ScrollType.INNER == scroll.getType()) {
            log.info(SCROLL_LOCATOR, scroll.getLocator());
            log.info(LOCATOR_STRATEGY, scroll.getLocatorStrategy());
        }
    }

    public void logHover(final Hover hover) {
        if (hover.isMoveToEmptySpace()) {
            log.info(MOVE_TO_EMPTY_SPACE, hover.isMoveToEmptySpace());
        }
    }

    public void logHotKeyInfo(final AbstractUiCommand command, final int position) {
        log.info(COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
    }

    public void logSingleKeyCommandTimes(final int times) {
        if (times > 1) {
            log.info(HOTKEY_COMMAND_TIMES, times);
        }
    }

    public void logCloseOrSwitchTabCommand(final String command, final Integer tabNumber) {
        log.info(TAB_COMMAND, command);
        log.info(TAB_INDEX, nonNull(tabNumber) ? tabNumber : LAST_TAB);
    }

    public void logOpenTabCommand(final String url) {
        log.info(TAB_COMMAND, OPEN_TAB);
        log.info(TAB_URL, isNotBlank(url) ? url : WITHOUT_URL);
    }

    public void logAssertCommand(final AbstractCommand command, final int position) {
        log.info(COMMAND_LOG, position, command.getClass().getSimpleName());
        log.info(COMMENT_LOG, command.getComment());
    }

    public void logAssertAttributeInfo(final AssertAttribute attribute) {
        log.info(NEGATIVE_LOG, attribute.isNegative());
        log.info(LOCATOR_LOG, attribute.getLocator());
        log.info(ATTRIBUTE_LOG, attribute.getName());
        log.info(CONTENT_LOG, StringPrettifier.cut(attribute.getContent()));
    }

    public void logAssertTitleCommand(final AssertTitle title) {
        log.info(NEGATIVE_LOG, title.isNegative());
        log.info(CONTENT_LOG, title.getContent());
    }

    public void logAssertAlertCommand(final AssertAlert alert) {
        log.info(NEGATIVE_LOG, alert.isNegative());
        log.info(CONTENT_LOG, alert.getText());
    }

    public void logDragAndDropInfo(final DragAndDrop dragAndDrop) {
        if (isNotBlank(dragAndDrop.getFileName())) {
            log.info(DRAGGING_FILE_PATH, dragAndDrop.getFileName());
        } else if (isNotBlank(dragAndDrop.getFromLocator())) {
            log.info(DRAGGING_FROM, dragAndDrop.getFromLocator());
        }
        log.info(DROPPING_TO, dragAndDrop.getToLocator());
    }

    public void logDragAndDropNativeInfo(final DragAndDropNative dragAndDropNative) {
        log.info(DRAGGING_FROM, dragAndDropNative.getFromLocator());
        log.info(DROPPING_TO, dragAndDropNative.getToLocator());
    }

    public void logSwipeNativeInfo(final SwipeNative swipeNative) {
        if (swipeNative.getElement() != null) {
            log.info(SWIPE_TYPE, "ELEMENT");
            log.info(SWIPE_QUANTITY, swipeNative.getElement().getQuantity());
            log.info(SWIPE_DIRECTION, swipeNative.getElement().getDirection());
            log.info(SWIPE_VALUE, swipeNative.getElement().getPercent());
            log.info(LOCATOR_LOG, swipeNative.getElement().getLocator());
        } else {
            log.info(SWIPE_TYPE, "PAGE");
            log.info(SWIPE_QUANTITY, swipeNative.getPage().getQuantity());
            log.info(SWIPE_DIRECTION, swipeNative.getPage().getDirection());
            log.info(SWIPE_VALUE, swipeNative.getPage().getPercent());
        }
    }

    public void logAssertPresent(final AssertPresent command) {
        log.info(LOCATOR_LOG, command.getLocator());
        log.info(NEGATIVE_LOG, command.isNegative());
    }

    public void logAssertChecked(final AssertChecked command) {
        log.info(LOCATOR_LOG, command.getLocator());
        log.info(NEGATIVE_LOG, command.isNegative());
    }

    public static void logScenarioWithoutTags(final String scenarioPath) {
        log.warn(SCENARIO_WITH_EMPTY_TAG_LOG, scenarioPath);
    }
}
