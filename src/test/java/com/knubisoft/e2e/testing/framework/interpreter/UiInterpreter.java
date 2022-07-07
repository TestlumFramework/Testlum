package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.BrowserUtil;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
import com.knubisoft.e2e.testing.framework.util.JavascriptUtil;
import com.knubisoft.e2e.testing.framework.util.LogUtil;
import com.knubisoft.e2e.testing.framework.util.SeleniumUtil;
import com.knubisoft.e2e.testing.framework.util.WaitUtil;
import com.knubisoft.e2e.testing.model.scenario.AbstractCommand;
import com.knubisoft.e2e.testing.model.scenario.Assert;
import com.knubisoft.e2e.testing.model.scenario.Clear;
import com.knubisoft.e2e.testing.model.scenario.Click;
import com.knubisoft.e2e.testing.model.scenario.ClickMethod;
import com.knubisoft.e2e.testing.model.scenario.CloseSecondTab;
import com.knubisoft.e2e.testing.model.scenario.DropDown;
import com.knubisoft.e2e.testing.model.scenario.Input;
import com.knubisoft.e2e.testing.model.scenario.Javascript;
import com.knubisoft.e2e.testing.model.scenario.Navigate;
import com.knubisoft.e2e.testing.model.scenario.NavigateCommand;
import com.knubisoft.e2e.testing.model.scenario.OneValue;
import com.knubisoft.e2e.testing.model.scenario.RepeatUiCommand;
import com.knubisoft.e2e.testing.model.scenario.Scroll;
import com.knubisoft.e2e.testing.model.scenario.ScrollDirection;
import com.knubisoft.e2e.testing.model.scenario.ScrollMeasure;
import com.knubisoft.e2e.testing.model.scenario.ScrollTo;
import com.knubisoft.e2e.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.e2e.testing.model.scenario.TypeForOneValue;
import com.knubisoft.e2e.testing.model.scenario.Ui;
import com.knubisoft.e2e.testing.model.scenario.Wait;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.Select;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings.JS_FOLDER;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ASSERT_ACTUAL;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ASSERT_ATTRIBUTE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ASSERT_EXPECTED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.ASSERT_LOCATOR;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.BY_URL_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLEAR_ACTION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLEAR_ACTION_LOCATOR;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLICK_LOCATOR;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLICK_METHOD;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLOSE_TAB_ACTION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.CLOSE_TAB_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.COMMAND_TYPE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DROP_DOWN_LOCATOR;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DROP_DOWN_NOT_SUPPORTED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DROP_DOWN_ONE_VALUE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.DROP_DOWN_OPERATION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.EXECUTION_TIME_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.INPUT_LOCATOR;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_EXECUTION_OPERATION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_OPERATION_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAVIGATE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAVIGATE_URL;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.REPEAT_COMMAND;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.REPEAT_FINISHED_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.REPEAT_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_ACTION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TO_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SECOND_TAB_NOT_FOUND;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.TIMES_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.UI_COMMAND_EXEC_TIME;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.WAIT_COMMAND;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.VALUE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.e2e.testing.model.scenario.ClickMethod.JS;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.e2e.testing.model.scenario.ClickMethod.SELENIUM;
import static java.lang.String.format;


@Slf4j
@InterpreterForClass(Ui.class)
public class UiInterpreter extends AbstractSeleniumInterpreter<Ui> {

    private final Map<UiCommandPredicate, UiCommand> uiCommands;

    public UiInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
        Map<UiCommandPredicate, UiCommand> commands = new HashMap<>();
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
        commands.put(ui -> ui instanceof RepeatUiCommand, (ui, result) -> repeat((RepeatUiCommand) ui, result));
        this.uiCommands = Collections.unmodifiableMap(commands);
    }

    @Override
    protected void acceptImpl(final Ui o, final CommandResult result) {
        LogUtil.logUiAttributes(o.isClearCookiesAfterExecution(), o.getClearLocalStorageByKey());
        runCommands(o.getClickOrInputOrNavigate(), result);
        clearLocalStorage(dependencies.getWebDriver(), o.getClearLocalStorageByKey());
        clearCookies(dependencies.getWebDriver(), o.isClearCookiesAfterExecution());
    }

    private void runCommands(final List<AbstractCommand> commandList, final CommandResult result) {
        commandList.forEach(command -> uiCommands.keySet().stream()
                .filter(key -> key.test(command))
                .map(uiCommands::get)
                .peek(s -> LogUtil.logUICommand(dependencies.getPosition().incrementAndGet(), command))
                .forEach(method -> uiCommandExec(command, result, method)));
    }

    private void uiCommandExec(final AbstractCommand command, final CommandResult result,
                                      final UiInterpreter.UiCommand method) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            method.accept(command, result);
        } finally {
            long execTime = stopWatch.getTime();
            stopWatch.stop();
            result.put(UI_COMMAND_EXEC_TIME, execTime);
            log.info(EXECUTION_TIME_LOG, execTime);
        }
    }

    private void click(final Click click, final CommandResult result) {
        result.put(CLICK_LOCATOR, click.getLocatorId());
        WebElement webElement = getWebElement(click.getLocatorId());
        BrowserUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
        highlightElementIfRequired(click.isHighlight(), webElement);
        takeScreenshotIfRequired(result);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put(CLICK_METHOD, JS.value());
            JavascriptUtil.executeJsScript(element, CLICK_SCRIPT, dependencies.getWebDriver());
        } else {
            result.put(CLICK_METHOD, SELENIUM.value());
            element.click();
        }
    }

    private void execJsCommands(final Javascript o, final CommandResult result) {
        String filePath = o.getFile();
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        URL resource = getClass().getClassLoader().getResource(JS_FOLDER);
        String command = JavascriptUtil.readCommands(filePath, resource, fileSearcher);
        result.put(JS_EXECUTION_OPERATION, format(JS_OPERATION_INFO, filePath, command));
        JavascriptUtil.executeJsScript(command, dependencies.getWebDriver());
    }

    private void input(final Input input, final CommandResult result) {
        result.put(INPUT_LOCATOR, input.getLocatorId());
        WebElement webElement = getWebElement(input.getLocatorId());
        highlightElementIfRequired(input.isHighlight(), webElement);
        send(input, webElement, result);
        takeScreenshotIfRequired(result);
    }

    private void send(final Input input, final WebElement element, final CommandResult result) {
        String injected = inject(input.getValue());
        String text = SeleniumUtil.resolveSendKeysType(injected, dependencies.getFileSearcher(), element);
        result.put("value", text);
        log.info(VALUE_LOG, text);
        element.sendKeys(text);
    }

    private void navigate(final Navigate navigate, final CommandResult result) {
        NavigateCommand navigateCommand = navigate.getCommand();
        log.info(COMMAND_TYPE_LOG, navigateCommand.name());
        result.put(NAVIGATE, navigateCommand.value());
        switch (navigateCommand) {
            case BACK: dependencies.getWebDriver().navigate().back();
                break;
            case RELOAD: dependencies.getWebDriver().navigate().refresh();
                break;
            case TO: navigateTo(navigate.getPath(), result);
                break;
            default: throw new DefaultFrameworkException(format(NAVIGATE_NOT_SUPPORTED, navigateCommand.value()));
        }
        takeScreenshotIfRequired(result);
    }

    private void navigateTo(final String path, final CommandResult result) {
        String url = inject(dependencies.getGlobalTestConfiguration().getUi().getBaseUrl() + path);
        result.put(NAVIGATE_URL, url);
        log.info(BY_URL_LOG, url);
        dependencies.getWebDriver().navigate().to(url);
    }

    private void assertValues(final Assert aAssert, final CommandResult result) {
        String actual = getActualValue(aAssert, result);
        String expected = getExpectedValue(aAssert);
        result.put(ASSERT_ACTUAL, actual);
        result.put(ASSERT_EXPECTED, expected);
        newCompare()
                .withActual(actual)
                .withExpected(expected)
                .exec();
        takeScreenshotIfRequired(result);
    }

    private String getActualValue(final Assert aAssert, final CommandResult result) {
        result.put(ASSERT_LOCATOR, aAssert.getLocatorId());
        result.put(ASSERT_ATTRIBUTE, aAssert.getAttribute().value());
        WebElement webElement = getWebElement(aAssert.getLocatorId());
        BrowserUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
        String value = webElement.getAttribute(aAssert.getAttribute().value());
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }

    private String getExpectedValue(final Assert aAssert) {
        return aAssert.getContent()
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }

    private void dropDown(final DropDown dropDown, final CommandResult result) {
        String locatorId = dropDown.getLocatorId();
        result.put(DROP_DOWN_LOCATOR, locatorId);
        Select select = getSelectElement(locatorId);
        OneValue oneValue = dropDown.getOneValue();
        if (oneValue != null) {
            processOneValueFromDropDown(oneValue, select, result);
        } else {
            log.info(COMMAND_TYPE_LOG, "DESELECT ALL");
            select.deselectAll();
        }
    }

    private void processOneValueFromDropDown(final OneValue oneValue, final Select select, final CommandResult result) {
        TypeForOneValue type = oneValue.getType();
        SelectOrDeselectBy method = oneValue.getBy();
        String value = inject(oneValue.getValue());
        log.info(COMMAND_TYPE_LOG, type.name());
        log.info(VALUE_LOG, value);
        result.put(DROP_DOWN_ONE_VALUE, format(DROP_DOWN_OPERATION, type.value(), method.value(), value));
        if (type == TypeForOneValue.SELECT) {
            selectByMethod(select, method, value);
        } else {
            deselectByMethod(select, method, value);
        }
        takeScreenshotIfRequired(result);
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
        result.put(CLEAR_ACTION, format(CLEAR_ACTION_LOCATOR, locatorId));
        WebElement element = getWebElement(locatorId);
        BrowserUtil.waitForElementVisibility(dependencies.getWebDriver(), element);
        element.clear();
        highlightElementIfRequired(clear.isHighlight(), element);
        takeScreenshotIfRequired(result);
    }

    private void closeSecondTab(final CloseSecondTab closeSecondTab, final CommandResult result) {
        result.put(CLOSE_TAB_ACTION, CLOSE_TAB_INFO);
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
        long time = wait.getTime().longValue();
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        result.put(WAIT_COMMAND, time);
        WaitUtil.getTimeUnit(wait.getUnit(), result).sleep(time);
    }

    private void scroll(final Scroll scroll, final CommandResult result) {
        ScrollDirection direction = scroll.getDirection();
        ScrollMeasure measure = scroll.getMeasure();
        String value = scroll.getValue().toString();
        result.put(SCROLL_ACTION, format(SCROLL_INFO, direction.value(), measure.value(), value));
        takeScreenshotIfRequired(result);
        JavascriptUtil.executeJsScript(JavascriptUtil.getScrollScript(direction, value, measure),
                dependencies.getWebDriver());
    }

    private void scrollTo(final ScrollTo scrollTo, final CommandResult result) {
        String locatorId = scrollTo.getLocatorId();
        WebElement element = getWebElement(locatorId);
        result.put(SCROLL_ACTION, format(SCROLL_TO_INFO, locatorId));
        takeScreenshotIfRequired(result);
        JavascriptUtil.executeJsScript(element, SCROLL_TO_ELEMENT_SCRIPT, dependencies.getWebDriver());
    }

    public void repeat(final RepeatUiCommand repeat, final CommandResult result) {
        int times = repeat.getTimes().intValue();
        result.put(REPEAT_COMMAND, format(REPEAT_INFO, times));
        log.info(TIMES_LOG, times);
        IntStream.range(0, times).forEach(e -> runCommands(repeat.getClickOrInputOrNavigate(), result));
        log.info(REPEAT_FINISHED_LOG);
    }

    private void clearLocalStorage(final WebDriver driver, final String key) {
        if (StringUtils.isNotBlank(key)) {
            WebStorage webStorage = (WebStorage) driver;
            webStorage.getLocalStorage().removeItem(key);
        }
    }

    private void clearCookies(final WebDriver driver, final boolean clearCookies) {
        if (clearCookies) {
            driver.manage().deleteAllCookies();
        }
    }

    private interface UiCommandPredicate extends Predicate<AbstractCommand> { }
    private interface UiCommand extends BiConsumer<AbstractCommand, CommandResult> { }
}
