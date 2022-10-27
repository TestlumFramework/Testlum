package com.knubisoft.cott.testing.framework.interpreter.lib.ui;

import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.cott.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.FileSearcher;
import com.knubisoft.cott.testing.framework.util.ImageComparator;
import com.knubisoft.cott.testing.framework.util.ImageComparisonUtil;
import com.knubisoft.cott.testing.framework.util.JavascriptUtil;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.framework.util.WaitUtil;
import com.knubisoft.cott.testing.model.global_config.Settings;
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
import com.knubisoft.cott.testing.model.scenario.ScrollTo;
import com.knubisoft.cott.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.cott.testing.model.scenario.SwitchToFrame;
import com.knubisoft.cott.testing.model.scenario.TypeForOneValue;
import com.knubisoft.cott.testing.model.scenario.Wait;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
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
import java.io.IOException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.DROP_DOWN_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.SECOND_TAB_NOT_FOUND;
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
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SWITCH_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.TIME;
import static com.knubisoft.cott.testing.model.scenario.ClickMethod.JS;
import static java.lang.String.format;


@Slf4j
public abstract class AbstractUiInterpreter<T extends AbstractCommand> extends AbstractInterpreter<T> {

    private static final Pattern HTTP_PATTERN = Pattern.compile("https?://.+");
    private static final String MOVE_TO_EMPTY_SPACE = "//html";
    protected final Map<UiCommandPredicate, UiCommand> uiCommands = new HashMap<>();
    protected final WebDriver webDriver;
    protected final Settings uiSettings;
    private final boolean stopScenarioOnFailure;

    public AbstractUiInterpreter(final InterpreterDependencies dependencies,
                                 final WebDriver webDriver,
                                 final Settings uiSettings) {
        super(dependencies);
        uiCommands.put(ui -> ui instanceof RepeatUiCommand, (ui, result) -> repeat((RepeatUiCommand) ui, result));
        uiCommands.put(ui -> ui instanceof Click, (ui, result) -> click((Click) ui, result));
        uiCommands.put(ui -> ui instanceof Input, (ui, result) -> input((Input) ui, result));
        uiCommands.put(ui -> ui instanceof Assert, (ui, result) -> assertValues((Assert) ui, result));
        uiCommands.put(ui -> ui instanceof DropDown, (ui, result) -> dropDown((DropDown) ui, result));
        uiCommands.put(ui -> ui instanceof Clear, (ui, result) -> clear((Clear) ui, result));
        uiCommands.put(ui -> ui instanceof Wait, (ui, result) -> wait((Wait) ui, result));
        uiCommands.put(ui -> ui instanceof Scroll, (ui, result) -> scroll((Scroll) ui, result));
        uiCommands.put(ui -> ui instanceof ScrollTo, (ui, result) -> scrollTo((ScrollTo) ui, result));
        uiCommands.put(ui -> ui instanceof Image, (ui, result) -> compareImages((Image) ui, result));
        uiCommands.put(ui -> ui instanceof SwitchToFrame, (ui, result) -> switchToFrame((SwitchToFrame) ui, result));
        this.uiSettings = uiSettings;
        this.webDriver = webDriver;
        this.stopScenarioOnFailure = GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure();
    }

    @Override
    protected void acceptImpl(final AbstractCommand ui, final CommandResult result) {
//        LogUtil.logUiAttributes(ui.isClearCookiesAfterExecution(), ui.getClearLocalStorageByKey());
//        runCommands(ui.getClickOrInputOrAssert(), result);
//        clearLocalStorage(webDriver, ui.getClearLocalStorageByKey(), result);
//        clearCookies(webDriver, ui.isClearCookiesAfterExecution(), result);
    }

    protected void runCommands(final List<AbstractCommand> commandList,
                             final CommandResult result) {
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);

        commandList.forEach(command -> uiCommands.keySet().stream()
                .filter(key -> key.test(command))
                .map(uiCommands::get)
                .peek(s -> LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), command))
                .forEach(method -> processEachCommand(command, method, subCommandsResult))
        );
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachCommand(final AbstractCommand command,
                                    final UiCommand method,
                                    final List<CommandResult> subCommandsResult) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                command.getClass().getSimpleName(),
                command.getComment());
        executeUiCommand(command, subCommandResult, method);
        subCommandsResult.add(subCommandResult);
    }

    private void executeUiCommand(final AbstractCommand command,
                                  final CommandResult subCommandResult,
                                  final UiCommand method) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            method.accept(command, subCommandResult);
        } catch (Exception e) {
            ResultUtil.setExceptionResult(subCommandResult, e);
            LogUtil.logException(e);
            checkIfStopScenarioOnFailure(e);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            subCommandResult.setExecutionTime(execTime);
            log.info(EXECUTION_TIME_LOG, execTime);
        }
    }

    protected void click(final Click click, final CommandResult result) {
        result.put(CLICK_LOCATOR, click.getLocatorId());
        WebElement webElement = UiUtil.findWebElement(webDriver, click.getLocatorId());
        UiUtil.waitForElementVisibility(webDriver, webElement);
        UiUtil.highlightElementIfRequired(click.isHighlight(), webElement, webDriver);
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put(CLICK_METHOD, "javascript");
            JavascriptUtil.executeJsScript(element, CLICK_SCRIPT, webDriver);
        } else {
            result.put(CLICK_METHOD, "selenium");
            element.click();
        }
    }



    protected void input(final Input input, final CommandResult result) {
        result.put(INPUT_LOCATOR, input.getLocatorId());
        WebElement webElement = UiUtil.findWebElement(webDriver, input.getLocatorId());
        UiUtil.highlightElementIfRequired(input.isHighlight(), webElement, webDriver);
        String injected = inject(input.getValue());
        String value = UiUtil.resolveSendKeysType(injected, webElement, dependencies.getFile());
        result.put(INPUT_VALUE, value);
        log.info(VALUE_LOG, value);
        webElement.sendKeys(value);
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
    }


    protected void assertValues(final Assert aAssert, final CommandResult result) {
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
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
    }

    private String getActualValue(final Assert aAssert) {
        WebElement webElement = UiUtil.findWebElement(webDriver, aAssert.getLocatorId());
        UiUtil.waitForElementVisibility(webDriver, webElement);
        String value = UiUtil.getElementAttribute(webElement, aAssert.getAttribute());
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }

    protected void dropDown(final DropDown dropDown, final CommandResult result) {
        String locatorId = dropDown.getLocatorId();
        result.put(DROP_DOWN_LOCATOR, locatorId);
        Select select = new Select(UiUtil.findWebElement(webDriver, locatorId));
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
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
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

    protected void clear(final Clear clear, final CommandResult result) {
        String locatorId = clear.getLocatorId();
        result.put(CLEAR_LOCATOR, locatorId);
        WebElement element = UiUtil.findWebElement(webDriver, locatorId);
        UiUtil.waitForElementVisibility(webDriver, element);
        element.clear();
        UiUtil.highlightElementIfRequired(clear.isHighlight(), element, webDriver);
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
    }

    @SneakyThrows
    protected void wait(final Wait wait, final CommandResult result) {
        String time = inject(wait.getTime());
        result.put(TIME, time);
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        WaitUtil.getTimeUnit(wait.getUnit(), result).sleep(Long.parseLong(time));
    }

    private void scroll(final Scroll scroll, final CommandResult result) {
        ResultUtil.addScrollMetaData(scroll, result);
        LogUtil.logScrollInfo(scroll);
        JavascriptUtil.executeScrollScript(scroll, dependencies.getWebDriver());
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
    }

    protected void scrollTo(final ScrollTo scrollTo, final CommandResult result) {
        String locatorId = scrollTo.getLocatorId();
        WebElement element = UiUtil.findWebElement(webDriver, locatorId);
        result.put(SCROLL_LOCATOR, locatorId);
        takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
        JavascriptUtil.executeJsScript(element, SCROLL_TO_ELEMENT_SCRIPT, webDriver);
    }

    private void switchToFrame(final SwitchToFrame switchToFrame, final CommandResult result) {
        String locatorId = switchToFrame.getLocatorId();
        LogUtil.logSwitchToFrameInfo(locatorId);
        result.put(SWITCH_LOCATOR, locatorId);
        WebElement element = UiUtil.findWebElement(webDriver,locatorId);
        dependencies.getWebDriver().switchTo().frame(element);
    }

    @SneakyThrows
    protected void compareImages(final Image image, final CommandResult result) {
        LogUtil.logImageComparisonInfo(image);
        ResultUtil.addImageComparisonMetaData(image, result);
        File scenarioFile = dependencies.getFile();
        BufferedImage expectedImage = ImageIO
                .read(FileSearcher.searchFileFromDir(scenarioFile, image.getFile()));
        BufferedImage actualImage = UiUtil.getActualImage(webDriver, image, result);
        ImageComparisonResult comparisonResult = ImageComparator.compare(expectedImage, actualImage);
        ImageComparisonUtil
                .processImageComparisonResult(comparisonResult, image, scenarioFile.getParentFile(), result);
    }

    protected void repeat(final RepeatUiCommand repeat, final CommandResult result) {
        int times = repeat.getTimes().intValue();
        result.put(NUMBER_OF_REPETITIONS, times);
        log.info(TIMES_LOG, times);
        List<AbstractCommand> commandsForRepeat = repeat.getClickOrInputOrNavigate();
        ResultUtil.addCommandsForRepeat(commandsForRepeat, result);
        IntStream.range(0, times)
                .forEach(e -> runCommands(commandsForRepeat, result));
        log.info(REPEAT_FINISHED_LOG);
    }


        private void execJsCommands(final Javascript o, final CommandResult result) {
            String fileName = o.getFile();
            result.put(JS_FILE, fileName);
            log.info(JS_FILE_LOG, fileName);
            String command = JavascriptUtil.readCommands(fileName);
            JavascriptUtil.executeJsScript(command, webDriver);
        }

        private void hover(final Hovers hovers, final CommandResult result) {
            ResultUtil.addHoversMetaData(hovers, result);
            Actions actions = new Actions(webDriver);
            List<WebElement> webElements = hovers.getHover().stream()
                    .peek(hover -> LogUtil.logHover(dependencies.getPosition().incrementAndGet(), hover))
                    .map(hover -> UiUtil.findWebElement(webDriver, hover.getLocatorId()))
                    .collect(Collectors.toList());
            webElements.forEach(webElement -> {
                actions.moveToElement(webElement);
                actions.perform();
            });
            moveToEmptySpace(hovers.isMoveToEmptySpace(), actions, webDriver);
        }

        private void moveToEmptySpace(final Boolean isMoveToEmptySpace, final Actions actions, final WebDriver webDriver) {
            if (Objects.nonNull(isMoveToEmptySpace) && isMoveToEmptySpace) {
                WebElement element = webDriver.findElement(By.xpath(MOVE_TO_EMPTY_SPACE));
                actions.moveToElement(element);
                actions.perform();
            }
        }

        private void closeSecondTab(final CloseSecondTab closeSecondTab, final CommandResult result) {
            result.put(CLOSE_COMMAND, SECOND_TAB);
            List<String> windowHandles = new ArrayList<>(webDriver.getWindowHandles());
            if (windowHandles.size() <= 1) {
                throw new DefaultFrameworkException(SECOND_TAB_NOT_FOUND);
            }
            webDriver.switchTo().window(windowHandles.get(1));
            webDriver.close();
            webDriver.switchTo().window(windowHandles.get(0));
        }

        private void navigate(final Navigate navigate, final CommandResult result) {
            NavigateCommand navigateCommand = navigate.getCommand();
            log.info(COMMAND_TYPE_LOG, navigateCommand.name());
            result.put(NAVIGATE_TYPE, navigateCommand.value());
            switch (navigateCommand) {
                case BACK: webDriver.navigate().back();
                    break;
                case RELOAD: webDriver.navigate().refresh();
                    break;
                case TO: navigateTo(navigate.getPath(), result);
                    break;
                default: throw new DefaultFrameworkException(format(NAVIGATE_NOT_SUPPORTED, navigateCommand.value()));
            }
            takeScreenshotAndSaveIfRequired(result, uiSettings, webDriver);
        }

        private void navigateTo(final String path, final CommandResult result) {
            String url = inject(getUrl(path));
            result.put(NAVIGATE_URL, url);
            log.info(BY_URL_LOG, url);
            webDriver.navigate().to(url);
        }

        private String getUrl(final String path) {
            if (HTTP_PATTERN.matcher(path).matches()) {
                return path;
            }
            return dependencies.getGlobalTestConfiguration().getWeb().getBaseUrl() + path;
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

    private void checkIfStopScenarioOnFailure(final Exception e) {
        if (stopScenarioOnFailure) {
            throw new DefaultFrameworkException(e);
        }
    }

    protected void takeScreenshotAndSaveIfRequired(final CommandResult result, final Settings settings,
                                                   final WebDriver webDriver) {
        if (settings.getTakeScreenshots().isEnable()) {
            File screenshot = UiUtil.takeScreenshot(webDriver);
            File screenshotsFolder = new File(dependencies.getFile().getParent()
                    + TestResourceSettings.SCREENSHOT_FOLDER);
            tryToCopyScreenshotFileToFolder(screenshot, screenshotsFolder);
            UiUtil.putScreenshotToResult(result, screenshot);
        }
    }

    private void tryToCopyScreenshotFileToFolder(final File screenshot, final File screenshotsFolder) {
        try {
            copyScreenshotFileToFolder(screenshot, screenshotsFolder);
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }


    private void copyScreenshotFileToFolder(final File screenshot, final File screenshotsFolder) throws IOException {
        String screenshotFileName = format(TestResourceSettings.SCREENSHOT_NAME_TO_SAVE,
                LocalTime.now(),
                dependencies.getPosition().get());
        File newScreenshot = new File(screenshotsFolder.getPath(), screenshotFileName);
        FileUtils.copyFile(screenshot, newScreenshot);
    }

    interface UiCommandPredicate extends Predicate<AbstractCommand> { }
    interface UiCommand extends BiConsumer<AbstractCommand, CommandResult> { }
}

