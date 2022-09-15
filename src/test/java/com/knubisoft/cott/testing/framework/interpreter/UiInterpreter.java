package com.knubisoft.cott.testing.framework.interpreter;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.exception.InvalidArgumentException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.ImageComparator;
import com.knubisoft.cott.testing.framework.util.ImageComparisonUtil;
import com.knubisoft.cott.testing.framework.util.JavascriptUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.SeleniumUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.framework.util.WaitUtil;
import com.knubisoft.cott.testing.model.scenario.AbstractCommand;
import com.knubisoft.cott.testing.model.scenario.Assert;
import com.knubisoft.cott.testing.model.scenario.Clear;
import com.knubisoft.cott.testing.model.scenario.Click;
import com.knubisoft.cott.testing.model.scenario.ClickMethod;
import com.knubisoft.cott.testing.model.scenario.CloseSecondTab;
import com.knubisoft.cott.testing.model.scenario.DropDown;
import com.knubisoft.cott.testing.model.scenario.Hovers;
import com.knubisoft.cott.testing.model.scenario.Image;
import com.knubisoft.cott.testing.model.scenario.Input;
import com.knubisoft.cott.testing.model.scenario.Javascript;
import com.knubisoft.cott.testing.model.scenario.Navigate;
import com.knubisoft.cott.testing.model.scenario.NavigateCommand;
import com.knubisoft.cott.testing.model.scenario.OneValue;
import com.knubisoft.cott.testing.model.scenario.RepeatUiCommand;
import com.knubisoft.cott.testing.model.scenario.Scroll;
import com.knubisoft.cott.testing.model.scenario.ScrollDirection;
import com.knubisoft.cott.testing.model.scenario.ScrollMeasure;
import com.knubisoft.cott.testing.model.scenario.ScrollTo;
import com.knubisoft.cott.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.cott.testing.model.scenario.TypeForOneValue;
import com.knubisoft.cott.testing.model.scenario.Ui;
import com.knubisoft.cott.testing.model.scenario.Wait;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.Select;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.*;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.BY_URL_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.EXECUTION_TIME_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.JS_FILE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.REPEAT_FINISHED_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.TIMES_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ALL_VALUES_DESELECT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_ATTRIBUTE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_COOKIES_AFTER_EXECUTION;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_LOCAL_STORAGE_BY_KEY;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLICK_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLICK_METHOD;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLOSE_COMMAND;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.DROP_DOWN_FOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.DROP_DOWN_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.INPUT_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.INPUT_VALUE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.JS_FILE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NAVIGATE_TYPE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NAVIGATE_URL;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.NUMBER_OF_REPETITIONS;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SCROLL_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SECOND_TAB;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.TIME;
import static com.knubisoft.cott.testing.model.scenario.ClickMethod.JS;
import static java.lang.String.format;


@Slf4j
@InterpreterForClass(Ui.class)
public class UiInterpreter extends AbstractSeleniumInterpreter<Ui> {

    private static final String MOVE_TO_EMPTY_SPACE = "//html";
    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+");
    private final Map<UiCommandPredicate, UiCommand> uiCommands;

    public UiInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<UiCommandPredicate, UiCommand> commands = new HashMap<>();
        commands.put(ui -> ui instanceof RepeatUiCommand, (ui, result) -> repeat((RepeatUiCommand) ui, result));
        commands.put(ui -> ui instanceof Click, (ui, result) -> click((Click) ui, result));
        commands.put(ui -> ui instanceof Input, (ui, result) -> input((Input) ui, result));
        commands.put(ui -> ui instanceof Navigate, (ui, result) -> navigate((Navigate) ui, result));
        commands.put(ui -> ui instanceof Assert, (ui, result) -> assertValues((Assert) ui, result));
        commands.put(ui -> ui instanceof DropDown, (ui, result) -> dropDown((DropDown) ui, result));
        commands.put(ui -> ui instanceof Javascript, (ui, result) -> execJsCommands((Javascript) ui, result));
        commands.put(ui -> ui instanceof Clear, (ui, result) -> clear((Clear) ui, result));
        commands.put(ui -> ui instanceof Wait, (ui, result) -> wait((Wait) ui, result));
        commands.put(ui -> ui instanceof CloseSecondTab, (ui, result) -> closeSecondTab((CloseSecondTab) ui, result));
        commands.put(ui -> ui instanceof Scroll, (ui, result) -> scroll((Scroll) ui, result));
        commands.put(ui -> ui instanceof ScrollTo, (ui, result) -> scrollTo((ScrollTo) ui, result));
        commands.put(ui -> ui instanceof Hovers, (ui, result) -> hover((Hovers) ui, result));
        commands.put(ui -> ui instanceof Image, (ui, result) -> compareImages((Image) ui, result));
        this.uiCommands = Collections.unmodifiableMap(commands);
    }

    @Override
    protected void acceptImpl(final Ui o, final CommandResult result) {
        LogUtil.logUiAttributes(o.isClearCookiesAfterExecution(), o.getClearLocalStorageByKey());
        List<CommandResult> subCommandsResult = new LinkedList<>();
        runCommands(o.getClickOrInputOrNavigate(), result, subCommandsResult);
        clearLocalStorage(dependencies.getWebDriver(), o.getClearLocalStorageByKey(), result);
        clearCookies(dependencies.getWebDriver(), o.isClearCookiesAfterExecution(), result);
    }

    private void runCommands(final List<AbstractCommand> commandList,
                             final CommandResult result,
                             final List<CommandResult> subCommandsResult) {
        commandList.forEach(command -> uiCommands.keySet().stream()
                .filter(key -> key.test(command))
                .map(uiCommands::get)
                .peek(s -> LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), command))
                .forEach(method -> processEachCommand(command, method, subCommandsResult)));
        result.setSubCommandsResult(subCommandsResult);
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractCommand command, final UiInterpreter.UiCommand method,
                                    final List<CommandResult> subCommandsResult) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                command.getClass().getSimpleName(),
                command.getComment());
        uiCommandExec(command, subCommandResult, method);
        subCommandsResult.add(subCommandResult);
    }

    private void uiCommandExec(final AbstractCommand command, final CommandResult result,
                               final UiInterpreter.UiCommand method) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            method.accept(command, result);
        } catch (Exception e) {
            result.setSuccess(false);
            result.setException(e);
            LogUtil.logException(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            result.setExecutionTime(execTime);
            log.info(EXECUTION_TIME_LOG, execTime);
        }
    }

    private void click(final Click click, final CommandResult result) {
        result.put(CLICK_LOCATOR, click.getLocatorId());
        WebElement webElement = findWebElement(click.getLocatorId());
        UiUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
        highlightElementIfRequired(click.isHighlight(), webElement);
        takeScreenshotAndSaveIfRequired(result);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put(CLICK_METHOD, "javascript");
            JavascriptUtil.executeJsScript(element, CLICK_SCRIPT, dependencies.getWebDriver());
        } else {
            result.put(CLICK_METHOD, "selenium");
            element.click();
        }
    }

    private void execJsCommands(final Javascript o, final CommandResult result) {
        String fileName = o.getFile();
        result.put(JS_FILE, fileName);
        log.info(JS_FILE_LOG, fileName);
        String command = JavascriptUtil.readCommands(fileName);
        JavascriptUtil.executeJsScript(command, dependencies.getWebDriver());
    }

    private void hover(final Hovers hovers, final CommandResult result) {
        ResultUtil.addHoversMetaData(hovers, result);
        WebDriver driver = dependencies.getWebDriver();
        Actions actions = new Actions(driver);
        List<WebElement> webElements = hovers.getHover().stream()
                .peek(hover -> LogUtil.logHover(dependencies.getPosition().incrementAndGet(), hover))
                .map(hover -> findWebElement(hover.getLocatorId()))
                .collect(Collectors.toList());
        webElements.forEach(webElement -> {
            actions.moveToElement(webElement);
            actions.perform();
        });
        moveToEmptySpace(hovers.isMoveToEmptySpace(), actions, driver);
    }

    private void moveToEmptySpace(final Boolean isMoveToEmptySpace, final Actions actions, final WebDriver webDriver) {
        if (Objects.nonNull(isMoveToEmptySpace) && isMoveToEmptySpace) {
            WebElement element = webDriver.findElement(By.xpath(MOVE_TO_EMPTY_SPACE));
            actions.moveToElement(element);
            actions.perform();
        }
    }

    private void input(final Input input, final CommandResult result) {
        result.put(INPUT_LOCATOR, input.getLocatorId());
        WebElement webElement = findWebElement(input.getLocatorId());
        highlightElementIfRequired(input.isHighlight(), webElement);
        String injected = inject(input.getValue());
        String value = SeleniumUtil.resolveSendKeysType(injected, webElement, dependencies.getFile());
        result.put(INPUT_VALUE, value);
        log.info(VALUE_LOG, value);
        webElement.sendKeys(value);
        takeScreenshotAndSaveIfRequired(result);
    }

    private void navigate(final Navigate navigate, final CommandResult result) {
        NavigateCommand navigateCommand = navigate.getCommand();
        log.info(COMMAND_TYPE_LOG, navigateCommand.name());
        result.put(NAVIGATE_TYPE, navigateCommand.value());
        switch (navigateCommand) {
            case BACK: dependencies.getWebDriver().navigate().back();
                break;
            case RELOAD: dependencies.getWebDriver().navigate().refresh();
                break;
            case TO: navigateTo(navigate.getPath(), result);
                break;
            default: throw new DefaultFrameworkException(format(NAVIGATE_NOT_SUPPORTED, navigateCommand.value()));
        }
        takeScreenshotAndSaveIfRequired(result);
    }

    private void navigateTo(final String path, final CommandResult result) {
        String url = inject(getUrl(path));
        result.put(NAVIGATE_URL, url);
        log.info(BY_URL_LOG, url);
        dependencies.getWebDriver().navigate().to(url);
    }

    private String getUrl(final String path) {
        if (HTTP_PATTERN.matcher(path).matches()) {
            return path;
        }
        return dependencies.getGlobalTestConfiguration().getUi().getBaseUrl() + path;
    }

    private void assertValues(final Assert aAssert, final CommandResult result) {
        result.put(ASSERT_LOCATOR, aAssert.getLocatorId());
        result.put(ASSERT_ATTRIBUTE, aAssert.getAttribute());
        String actual = getActualValue(aAssert);
        String expected = aAssert.getContent().replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
        result.setActual(actual);
        result.setExpected(expected);
        newCompare()
                .withActual(actual)
                .withExpected(expected)
                .exec();
        takeScreenshotAndSaveIfRequired(result);
    }

    private String getActualValue(final Assert aAssert) {
        WebElement webElement = findWebElement(aAssert.getLocatorId());
        UiUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
        String value = webElement.getAttribute(aAssert.getAttribute());
        if (Objects.isNull(value)){
            throw new DefaultFrameworkException(WRONG_ATTRIBUTE, aAssert.getLocatorId(), aAssert.getAttribute());
        }
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }

    private void dropDown(final DropDown dropDown, final CommandResult result) {
        String locatorId = dropDown.getLocatorId();
        result.put(DROP_DOWN_LOCATOR, locatorId);
        Select select = new Select(findWebElement(locatorId));
        OneValue oneValue = dropDown.getOneValue();
        if (oneValue != null) {
            processOneValueFromDropDown(oneValue, select, result);
        } else {
            log.info(COMMAND_TYPE_LOG, ALL_VALUES_DESELECT);
            result.put(DROP_DOWN_FOR, ALL_VALUES_DESELECT);
            select.deselectAll();
        }
    }

    private void processOneValueFromDropDown(final OneValue oneValue, final Select select, final CommandResult result) {
        TypeForOneValue type = oneValue.getType();
        SelectOrDeselectBy method = oneValue.getBy();
        String value = inject(oneValue.getValue());
        ResultUtil.addDropDownForOneValueMetaData(type.value(), method.value(), value, result);
        log.info(COMMAND_TYPE_LOG, type.value());
        log.info(VALUE_LOG, value);
        if (type == TypeForOneValue.SELECT) {
            selectByMethod(select, method, value);
        } else {
            deselectByMethod(select, method, value);
        }
        takeScreenshotAndSaveIfRequired(result);
    }

    private void selectByMethod(final Select select, final SelectOrDeselectBy method, final String value) {
        switch (method) {
            case INDEX: select.selectByIndex(Integer.parseInt(value));
                break;
            case TEXT: select.selectByVisibleText(value);
                break;
            case VALUE: select.selectByValue(value);
                break;
            default: throw new DefaultFrameworkException(format(DROP_DOWN_NOT_SUPPORTED, method.value()));
        }
    }

    private void deselectByMethod(final Select select, final SelectOrDeselectBy method, final String value) {
        switch (method) {
            case INDEX: select.deselectByIndex(Integer.parseInt(value));
                break;
            case TEXT: select.deselectByVisibleText(value);
                break;
            case VALUE: select.deselectByValue(value);
                break;
            default: throw new DefaultFrameworkException(format(DROP_DOWN_NOT_SUPPORTED, method.value()));
        }
    }

    private void clear(final Clear clear, final CommandResult result) {
        String locatorId = clear.getLocatorId();
        result.put(CLEAR_LOCATOR, locatorId);
        WebElement element = findWebElement(locatorId);
        UiUtil.waitForElementVisibility(dependencies.getWebDriver(), element);
        element.clear();
        highlightElementIfRequired(clear.isHighlight(), element);
        takeScreenshotAndSaveIfRequired(result);
    }

    private void closeSecondTab(final CloseSecondTab closeSecondTab, final CommandResult result) {
        result.put(CLOSE_COMMAND, SECOND_TAB);
        WebDriver webDriver = dependencies.getWebDriver();
        List<String> windowHandles = new ArrayList<>(webDriver.getWindowHandles());
        if (windowHandles.size() <= 1) {
            throw new DefaultFrameworkException(SECOND_TAB_NOT_FOUND);
        }
        webDriver.switchTo().window(windowHandles.get(1));
        webDriver.close();
        webDriver.switchTo().window(windowHandles.get(0));
    }

    @SneakyThrows
    private void wait(final Wait wait, final CommandResult result) {
        String time = inject(wait.getTime());
        result.put(TIME, time);
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        WaitUtil.getTimeUnit(wait.getUnit(), result).sleep(Long.parseLong(time));
    }

    private void scroll(final Scroll scroll, final CommandResult result) {
        ScrollDirection direction = scroll.getDirection();
        ScrollMeasure measure = scroll.getMeasure();
        String value = scroll.getValue().toString();
        ResultUtil.addScrollMetaData(direction.value(), measure.value(), value, result);
        takeScreenshotAndSaveIfRequired(result);
        JavascriptUtil.executeJsScript(JavascriptUtil.getScrollScript(direction, value, measure),
                dependencies.getWebDriver());
    }

    private void scrollTo(final ScrollTo scrollTo, final CommandResult result) {
        String locatorId = scrollTo.getLocatorId();
        WebElement element = findWebElement(locatorId);
        result.put(SCROLL_LOCATOR, locatorId);
        takeScreenshotAndSaveIfRequired(result);
        JavascriptUtil.executeJsScript(element, SCROLL_TO_ELEMENT_SCRIPT, dependencies.getWebDriver());
    }

    @SneakyThrows
    private void compareImages(final Image image, final CommandResult result) {
        LogUtil.logImageComparisonInfo(image);
        ResultUtil.addImageComparisonMetaData(image, result);
        File scenarioFile = dependencies.getFile();
        BufferedImage expectedImage = ImageIO
                .read(FileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actualImage = UiUtil.getActualImage(dependencies.getWebDriver(), image, result);
        ImageComparisonResult comparisonResult = ImageComparator.compare(expectedImage, actualImage);
        ImageComparisonUtil
                .processImageComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
    }

    private void repeat(final RepeatUiCommand repeat, final CommandResult result) {
        int times = repeat.getTimes().intValue();
        result.put(NUMBER_OF_REPETITIONS, times);
        log.info(TIMES_LOG, times);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        List<AbstractCommand> commandsForRepeat = repeat.getClickOrInputOrNavigate();
        ResultUtil.addCommandsForRepeat(commandsForRepeat, result);
        IntStream.range(0, times)
                .forEach(e -> runCommands(commandsForRepeat, result, subCommandsResult));
        log.info(REPEAT_FINISHED_LOG);
    }

    private void clearLocalStorage(final WebDriver driver, final String key, final CommandResult result) {
        if (StringUtils.isNotEmpty(key)) {
            result.put(CLEAR_LOCAL_STORAGE_BY_KEY, key);
            WebStorage webStorage = (WebStorage) driver;
            webStorage.getLocalStorage().removeItem(key);
        }
    }

    private void clearCookies(final WebDriver driver, final boolean clearCookies, final CommandResult result) {
        result.put(CLEAR_COOKIES_AFTER_EXECUTION, clearCookies);
        if (clearCookies) {
            driver.manage().deleteAllCookies();
        }
    }

    private interface UiCommandPredicate extends Predicate<AbstractCommand> { }
    private interface UiCommand extends BiConsumer<AbstractCommand, CommandResult> { }
}
