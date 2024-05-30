package com.knubisoft.testlum.testing.framework.util;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertChecked;
import com.knubisoft.testlum.testing.model.scenario.AssertPresent;
import com.knubisoft.testlum.testing.model.scenario.DragAndDrop;
import com.knubisoft.testlum.testing.model.scenario.DragAndDropNative;
import com.knubisoft.testlum.testing.model.scenario.Exclude;
import com.knubisoft.testlum.testing.model.scenario.FromSQL;
import com.knubisoft.testlum.testing.model.scenario.FullScreen;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import com.knubisoft.testlum.testing.model.scenario.Image;
import com.knubisoft.testlum.testing.model.scenario.MobileImage;
import com.knubisoft.testlum.testing.model.scenario.NativeImage;
import com.knubisoft.testlum.testing.model.scenario.Part;
import com.knubisoft.testlum.testing.model.scenario.Picture;
import com.knubisoft.testlum.testing.model.scenario.Scroll;
import com.knubisoft.testlum.testing.model.scenario.ScrollType;
import com.knubisoft.testlum.testing.model.scenario.SwipeNative;
import com.knubisoft.testlum.testing.model.scenario.WebFullScreen;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.X;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.EXTRACT_THEN_COMPARE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.TAKE_SCREENSHOT_THEN_COMPARE;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.isNotBlank;

@UtilityClass
public class ResultUtil {

    public static final String ALIAS = "Alias";
    public static final String API_ALIAS = "API alias";
    public static final String JSON_PATH = "JSON path";
    public static final String XML_PATH = "Xml path";
    public static final String RELATIONAL_DB_QUERY = "Relational DB query";
    public static final String FILE = "File";
    public static final String EXPRESSION = "Expression";
    public static final String CONSTANT = "Constant";
    public static final String CONTENT = "Content";
    public static final String NO_EXPRESSION = "No expression";
    public static final String COOKIES = "Cookies";
    public static final String URL = "Url";
    public static final String HTML_DOM = "HTML Dom";
    public static final String FULL_DOM = "Full Dom";
    public static final String LOCATOR_ID = "Locator ID";
    public static final String LOCATOR_FORM = "Locator ID = %s";
    public static final String ELEMENT_PRESENT = "Is the web element present";
    public static final String ELEMENT_ATTRIBUTE = "Value of element attribute";
    public static final String CONDITION = "Condition";
    public static final String GENERATED_STRING = "Randomly generated string";
    public static final String ASSERT_LOCATOR = "Locator for assert command";
    public static final String ASSERT_ATTRIBUTE = "Assert command attribute";
    public static final String ASSERT_NEGATIVE = "Is assert negative";
    public static final String CLICK_LOCATOR = "Locator for click command";
    public static final String DOUBLE_CLICK_LOCATOR = "Locator for click command";
    public static final String INPUT_LOCATOR = "Locator for input command";
    public static final String CLEAR_LOCATOR = "Locator for clear command";
    public static final String SCROLL_LOCATOR = "Locator for scroll-to command";
    public static final String SWITCH_LOCATOR = "Locator for switch command";
    public static String SWITCH_INDEX = "Index for switch command";
    public static final String HOTKEY_LOCATOR = "Locator for hotkey command";
    public static final String HOTKEY_TIMES = "Times to repeat hotkey command";
    public static final String INPUT_VALUE = "Value for input";
    public static final String CLICK_METHOD = "Click method";
    public static final String CLOSE_TAB = "Close";
    public static final String SWITCH_TAB = "Switch";
    public static final String OPEN_TAB = "Open";
    public static final String CLOSE_COMMAND = "Close command for";
    public static final String SWITCH_COMMAND = "Switch command for";
    public static final String OPEN_COMMAND = "Open command for";
    public static final String LAST_TAB = "Last opened tab";
    public static final String NEW_TAB = "Newly created tab";
    public static final String NEW_TAB_WITH_URL = "Newly created tab with url: %s";
    public static final String WITHOUT_URL = "Without url";
    public static final String TAB_WITH_INDEX = "Tab with index '%s'";
    public static final String JS_FILE = "JS file to execute";
    public static final String NAVIGATE_TYPE = "Navigate command type";
    public static final String NAVIGATE_URL = "URL for navigate";
    public static final String DROP_DOWN_LOCATOR = "Locator for drop down command";
    public static final String DROP_DOWN_FOR = "Drop down command for";
    public static final String DROP_DOWN_BY = "Process by";
    public static final String NATIVE_NAVIGATE_TO = "Navigate to destination";
    public static final String ALL_VALUES_DESELECT = "all values (deselect)";
    public static final String ALL_VALUES_SELECT = "all values (select)";
    public static final String ONE_VALUE_TEMPLATE = "one value (%s)";
    public static final String URL_TO_ACTUAL_IMAGE = "URL to actual image";
    public static final String ADDITIONAL_INFO = "Additional info";
    public static final String IMAGE_ATTACHED_TO_STEP = "Actual image attached to report step";
    public static final String IMAGE_MISMATCH_PERCENT = "Expected image size";
    private static final String FROM_LOCATOR = "From element with locator";
    private static final String FROM_LOCAL_FILE = "From local file";
    private static final String TO_LOCATOR = "To element with locator";
    private static final String PERFORM_SWIPE = "Perform swipe with direction";
    private static final String SWIPE_VALUE = "Swipe value in percent due to screen dimensions";
    private static final String SWIPE_QUANTITY = "Quantity of swipes";
    private static final String SWIPE_TYPE = "Swipe type";
    private static final String SWIPE_LOCATOR = "Locator for swipe";
    private static final String SCROLL_DIRECTION = "Scroll direction";
    private static final String SCROLL_MEASURE = "Scroll measure";
    private static final String SCROLL_TYPE = "Scroll type";
    private static final String LOCATOR_FOR_SCROLL = "Locator for scroll";
    private static final String LOCATOR_STRATEGY = "Locator strategy";
    private static final String ENDPOINT = "Endpoint";
    private static final String HTTP_METHOD = "HTTP method";
    private static final String ADDITIONAL_HEADERS = "Additional headers";
    private static final String TYPE = "Type";
    private static final String DB_TYPE = "DB type";
    private static final String NAME = "Name";
    private static final String VALUE = "Value";
    private static final String TIME = "Time";
    private static final String TIME_UNITE = "Time unit";
    private static final String HEADER_TEMPLATE = "%s: %s";
    private static final String MOVE_TO_EMPTY_SPACE = "Move to empty space after execution";
    private static final String STEP_FAILED = "Step failed";
    private static final String FAILED = "failed";
    private static final String SUCCESSFULLY = "successfully";
    private static final String EXECUTION_RESULT_FILENAME = "scenarios_execution_result.txt";
    private static final String IMAGE_FOR_COMPARISON = "Image for comparison";
    private static final String HIGHLIGHT_DIFFERENCE = "Highlight difference";
    private static final String IMAGE_COMPARISON_TYPE = "Image comparison type";
    private static final String IMAGE_LOCATOR = "Locator to element with picture";
    private static final String IMAGE_SOURCE_ATT = "Image source attribute name";
    private static final String MATCH_PERCENTAGE = "Match percentage";
    private static final String EXCLUDED_ELEMENT = "Excluded elements locators";
    private static final String ACTUAL_IMAGE_SIZE = "Actual image size";
    private static final String EXPECTED_IMAGE_SIZE = "Expected image size";

    public CommandResult newCommandResultInstance(final int number, final AbstractCommand... command) {
        CommandResult commandResult = new CommandResult();
        commandResult.setId(number);
        commandResult.setSuccess(true);
        if (nonNull(command) && command.length > 0) {
            commandResult.setCommandKey(command[0].getClass().getSimpleName());
        }
        return commandResult;
    }

    public CommandResult newUiCommandResultInstance(final int number, final AbstractCommand command) {
        CommandResult commandResult = newCommandResultInstance(number, command);
        commandResult.setComment(command.getComment());
        return commandResult;
    }

    public void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult
                    .stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    public void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    public void setExpectedActual(final String expected, final String actual, final CommandResult result) {
        result.setExpected(expected);
        result.setActual(actual);
    }

    public void addHttpMetaData(final String alias,
                                final String httpMethodName,
                                final Map<String, String> headers,
                                final String endpoint,
                                final CommandResult result) {
        result.put(API_ALIAS, alias);
        result.put(ENDPOINT, endpoint);
        result.put(HTTP_METHOD, httpMethodName);
        if (!headers.isEmpty()) {
            addHeadersMetaData(headers, result);
        }
    }

    private void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .collect(Collectors.toList()));
    }

    public void addVariableMetaData(final String type,
                                    final String key,
                                    final String expression,
                                    final String value,
                                    final CommandResult result) {
        result.put(TYPE, type);
        result.put(NAME, key);
        result.put(EXPRESSION, expression);
        result.put(VALUE, value);
    }

    public void addVariableMetaData(final String type,
                                    final String key,
                                    final String format,
                                    final String expression,
                                    final String value,
                                    final CommandResult result) {
        addVariableMetaData(type, key, format(format, expression), value, result);
    }

    public void addVariableMetaData(final String queryType,
                                    final FromSQL fromSQL,
                                    final String key,
                                    final String value,
                                    final CommandResult result) {
        result.put(DB_TYPE, fromSQL.getDbType().name());
        result.put(ALIAS, fromSQL.getAlias());
        addVariableMetaData(queryType, key, fromSQL.getQuery(), value, result);
    }

    public void addConditionMetaData(final String key,
                                     final String expression,
                                     final Boolean value,
                                     final CommandResult result) {
        result.put(NAME, key);
        result.put(EXPRESSION, expression);
        result.put(VALUE, value);
    }

    public void addCommandOnConditionMetaData(final String conditionName,
                                              final Boolean conditionResult,
                                              final CommandResult result) {
        result.setSkipped(!conditionResult);
        result.put(CONDITION, conditionName + " = " + conditionResult);
    }

    public void addWaitMetaData(final String time,
                                final TimeUnit unit,
                                final CommandResult result) {
        result.put(TIME, time);
        result.put(TIME_UNITE, unit.name());
    }

    public void addDropDownForOneValueMetaData(final String type,
                                               final String processBy,
                                               final String value,
                                               final CommandResult result) {
        result.put(DROP_DOWN_FOR, format(ONE_VALUE_TEMPLATE, type));
        result.put(DROP_DOWN_BY, processBy);
        result.put(VALUE, value);
    }

    public void addScrollMetaData(final Scroll scroll,
                                  final CommandResult result) {
        result.put(SCROLL_DIRECTION, scroll.getDirection());
        result.put(SCROLL_MEASURE, scroll.getMeasure());
        result.put(VALUE, scroll.getValue());
        result.put(SCROLL_TYPE, scroll.getType());
        if (ScrollType.INNER == scroll.getType()) {
            result.put(LOCATOR_FOR_SCROLL, scroll.getLocator());
            result.put(LOCATOR_STRATEGY, scroll.getLocatorStrategy());
        }
    }

    public void addDragAndDropMetaDada(final DragAndDrop dragAndDrop,
                                       final CommandResult result) {
        if (isNotBlank(dragAndDrop.getFileName())) {
            result.put(FROM_LOCAL_FILE, dragAndDrop.getFileName());
        } else if (isNotBlank(dragAndDrop.getFromLocator())) {
            result.put(FROM_LOCATOR, dragAndDrop.getFromLocator());
        }
        result.put(TO_LOCATOR, dragAndDrop.getToLocator());
    }

    public void addDragAndDropNativeMetaDada(final DragAndDropNative dragAndDropNative,
                                             final CommandResult result) {
        result.put(FROM_LOCATOR, dragAndDropNative.getFromLocator());
        result.put(TO_LOCATOR, dragAndDropNative.getToLocator());
    }

    public void addHoverMetaData(final Hover hover, final CommandResult result) {
        result.setComment(hover.getComment());
        result.put(LOCATOR_ID, hover.getLocator());
        result.put(MOVE_TO_EMPTY_SPACE, hover.isMoveToEmptySpace());
    }

    public void addSingleKeyCommandMetaData(final int times, final CommandResult result) {
        if (times > 1) {
            result.put(HOTKEY_TIMES, times);
        }
    }

    public void addCloseOrSwitchTabMetadata(final String commandName,
                                            final Integer tabIndex,
                                            final CommandResult result) {
        result.put(commandName, nonNull(tabIndex) ? String.format(TAB_WITH_INDEX, tabIndex) : LAST_TAB);
    }

    public void addOpenTabMetadata(final String url,
                                   final CommandResult result) {
        result.put(OPEN_COMMAND, isNotBlank(url) ? String.format(NEW_TAB_WITH_URL, url) : NEW_TAB);
    }

    @SneakyThrows
    public void writeFullTestCycleExecutionResult(final TestExecutionSummary testExecutionSummary) {
        File executionResultFile = new File(TestResourceSettings.getInstance().getTestResourcesFolder(),
                EXECUTION_RESULT_FILENAME);
        String result = CollectionUtils.isNotEmpty(testExecutionSummary.getFailures())
                || testExecutionSummary.getTestsAbortedCount() > 0 ? FAILED : SUCCESSFULLY;
        FileUtils.write(executionResultFile, result, StandardCharsets.UTF_8);
    }

    public void addImageComparisonMetaData(final Image image, final CommandResult result) {
        result.put(IMAGE_FOR_COMPARISON, image.getFile());
        result.put(HIGHLIGHT_DIFFERENCE, image.isHighlightDifference());
        if (nonNull(image.getPicture())) {
            addCompareWithElementMetaData(image.getPicture(), result);
        } else if (nonNull(image.getFullScreen())) {
            addCompareWithFullScreenMetaData(image.getFullScreen(), result);
        } else if (nonNull(image.getPart())) {
            addCompareWithPartMetaData(image.getPart(), result);
        }
    }

    public void addImageComparisonMetaData(final MobileImage image, final CommandResult result) {
        result.put(IMAGE_FOR_COMPARISON, image.getFile());
        result.put(HIGHLIGHT_DIFFERENCE, image.isHighlightDifference());
        if (nonNull(image.getPicture())) {
            addCompareWithElementMetaData(image.getPicture(), result);
        } else if (nonNull(image.getFullScreen())) {
            addCompareWithFullScreenMetaData(image.getFullScreen(), result);
        } else if (nonNull(image.getPart())) {
            addCompareWithPartMetaData(image.getPart(), result);
        }
    }

    public void addImageComparisonMetaData(final NativeImage image, final CommandResult result) {
        result.put(IMAGE_FOR_COMPARISON, image.getFile());
        result.put(HIGHLIGHT_DIFFERENCE, image.isHighlightDifference());
        if (nonNull(image.getFullScreen())) {
            addCompareWithFullScreenMetaData(image.getFullScreen(), result);
        } else if (nonNull(image.getPart())) {
            addCompareWithPartMetaData(image.getPart(), result);
        }
    }

    private void addCompareWithElementMetaData(final Picture element, final CommandResult result) {
        result.put(IMAGE_COMPARISON_TYPE, EXTRACT_THEN_COMPARE);
        result.put(IMAGE_LOCATOR, element.getLocator());
        result.put(IMAGE_SOURCE_ATT, element.getAttribute());
    }

    private void addCompareWithFullScreenMetaData(final WebFullScreen fullScreen, final CommandResult result) {
        result.put(IMAGE_COMPARISON_TYPE, TAKE_SCREENSHOT_THEN_COMPARE);
        if (nonNull(fullScreen.getPercentage())) {
            result.put(MATCH_PERCENTAGE, fullScreen.getPercentage());
        }
        if (!fullScreen.getExclude().isEmpty()) {
            result.put(EXCLUDED_ELEMENT, StringUtils.join(fullScreen.getExclude().stream()
                    .map(Exclude::getLocator)
                    .collect(Collectors.joining(COMMA + SPACE))));
        }
    }

    private void addCompareWithFullScreenMetaData(final FullScreen fullScreen, final CommandResult result) {
        result.put(IMAGE_COMPARISON_TYPE, TAKE_SCREENSHOT_THEN_COMPARE);
        if (nonNull(fullScreen.getPercentage())) {
            result.put(MATCH_PERCENTAGE, fullScreen.getPercentage());
        }
    }

    private void addCompareWithPartMetaData(final Part part,
                                            final CommandResult result) {
        result.put(IMAGE_COMPARISON_TYPE, GET_ELEMENT_AS_SCREENSHOT_THEN_COMPARE);
        result.put(IMAGE_LOCATOR, part.getLocator());
        if (nonNull(part.getPercentage())) {
            result.put(MATCH_PERCENTAGE, part.getPercentage());
        }
    }

    public void addImagesSizeMetaData(final ImageComparisonResult comparisonResult, final CommandResult result) {
        result.put(EXPECTED_IMAGE_SIZE, comparisonResult.getExpected().getWidth()
                + X + comparisonResult.getExpected().getHeight());
        result.put(ACTUAL_IMAGE_SIZE, comparisonResult.getActual().getWidth()
                + X + comparisonResult.getActual().getHeight());
    }

    public void addAssertAttributeMetaData(final AssertAttribute attribute, final CommandResult result) {
        result.put(ASSERT_LOCATOR, attribute.getLocator());
        result.put(ASSERT_ATTRIBUTE, attribute.getName());
        result.put(ASSERT_NEGATIVE, attribute.isNegative());
    }

    public void addAssertPresentMetadata(final AssertPresent present, final CommandResult result) {
        result.put(ASSERT_LOCATOR, present.getLocator());
        result.put(ASSERT_NEGATIVE, present.isNegative());
    }

    public void addAssertCheckedMetadata(final AssertChecked assertChecked, final CommandResult result) {
        result.put(ASSERT_LOCATOR, assertChecked.getLocator());
        result.put(ASSERT_NEGATIVE, assertChecked.isNegative());
    }

    public void addSwipeMetaData(final SwipeNative swipeNative, final CommandResult result) {
        result.put(SWIPE_TYPE, swipeNative.getType().value());
        result.put(SWIPE_QUANTITY, swipeNative.getQuantity());
        result.put(PERFORM_SWIPE, swipeNative.getDirection());
        result.put(SWIPE_VALUE, swipeNative.getPercent());
        if (isNotBlank(swipeNative.getLocator())) {
            result.put(SWIPE_LOCATOR, swipeNative.getLocator());
        }
    }

}
