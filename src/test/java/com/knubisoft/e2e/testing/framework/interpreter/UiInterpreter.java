package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.ExplicitWaitUtil;
import com.knubisoft.e2e.testing.framework.util.FileSearcher;
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
import com.knubisoft.e2e.testing.model.scenario.Horizontal;
import com.knubisoft.e2e.testing.model.scenario.Input;
import com.knubisoft.e2e.testing.model.scenario.Javascript;
import com.knubisoft.e2e.testing.model.scenario.Navigate;
import com.knubisoft.e2e.testing.model.scenario.NavigateCommand;
import com.knubisoft.e2e.testing.model.scenario.OneValue;
import com.knubisoft.e2e.testing.model.scenario.Scroll;
import com.knubisoft.e2e.testing.model.scenario.ScrollBy;
import com.knubisoft.e2e.testing.model.scenario.ScrollHorizontalDir;
import com.knubisoft.e2e.testing.model.scenario.ScrollToElementBy;
import com.knubisoft.e2e.testing.model.scenario.ScrollToTopOrBottom;
import com.knubisoft.e2e.testing.model.scenario.ScrollVerticalDir;
import com.knubisoft.e2e.testing.model.scenario.SelectOrDeselectBy;
import com.knubisoft.e2e.testing.model.scenario.ToElement;
import com.knubisoft.e2e.testing.model.scenario.TypeForOneValue;
import com.knubisoft.e2e.testing.model.scenario.Ui;
import com.knubisoft.e2e.testing.model.scenario.Vertical;
import com.knubisoft.e2e.testing.model.scenario.VerticalAndHorizontal;
import com.knubisoft.e2e.testing.model.scenario.VerticalByValue;
import com.knubisoft.e2e.testing.model.scenario.Wait;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.knubisoft.e2e.testing.framework.configuration.TestResourceSettings.JS_FOLDER;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_HORIZONTAL_PERCENT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_HORIZONTAL_SCRIPT_FORMAT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_TO_BOTTOM_SCRIPT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_TO_TOP_SCRIPT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_AND_HORIZONTAL_FORMAT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_PERCENT;
import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.SCROLL_VERTICAL_SCRIPT_FORMAT;
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
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_FILE_NOT_FOUND;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_FILE_UNREADABLE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.JS_OPERATION_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAVIGATE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAVIGATE_NOT_SUPPORTED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.NAVIGATE_URL;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_ACTION;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_BY_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_DIRECTION_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TOP_OR_BOTTOM_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TO_DIRECTION_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TO_ELEMENT_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TO_ELEMENT_NOT_SUPPORTED;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_TYPE_LOG;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SCROLL_V_AND_H_INFO;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.SECOND_TAB_NOT_FOUND;
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
    private final int percentage = 100;

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
        this.uiCommands = Collections.unmodifiableMap(commands);
    }

    @Override
    protected void acceptImpl(final Ui o, final CommandResult result) {
        o.getClickOrInputOrNavigate().forEach(command -> uiCommands.keySet().stream()
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
        ExplicitWaitUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
        highlightElementIfRequired(click.isHighlight(), webElement);
        takeScreenshotIfRequired(result);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put(CLICK_METHOD, JS.value());
            executeJsScript(element, result, CLICK_SCRIPT);
        } else {
            result.put(CLICK_METHOD, SELENIUM.value());
            element.click();
        }
    }

    private void executeJsScript(final WebElement element, final CommandResult result, final String script) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) dependencies.getWebDriver();
        takeScreenshotIfRequired(result);
        javascriptExecutor.executeScript(script, element);
    }

    private void executeJsScriptWithoutWebEl(final CommandResult result, final String script) {
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) dependencies.getWebDriver();
        takeScreenshotIfRequired(result);
        javascriptExecutor.executeScript(script);
    }

    private void execJsCommands(final Javascript o, final CommandResult result) {
        WebDriver driver = dependencies.getWebDriver();
        String filePath = o.getFile();
        String command = readCommands(filePath);
        result.put(JS_EXECUTION_OPERATION, format(JS_OPERATION_INFO, filePath, command));

        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;
        javascriptExecutor.executeScript(command);
    }

    private String readCommands(final String filePath) {
        try {
            File jsFile = getJsFileByPath(filePath);
            List<String> commands = Files.readAllLines(jsFile.toPath());
            return String.join(EMPTY, commands);
        } catch (IOException e) {
            throw new DefaultFrameworkException(format(JS_FILE_UNREADABLE, filePath));
        }
    }

    private File getJsFileByPath(final String filePath) {
        FileSearcher fileSearcher = dependencies.getFileSearcher();
        URL resource = getClass().getClassLoader().getResource(JS_FOLDER);
        try {
            File fromDir = new File(Objects.requireNonNull(resource).toURI());
            return fileSearcher.search(fromDir, filePath);
        } catch (URISyntaxException e) {
            throw new DefaultFrameworkException(format(JS_FILE_NOT_FOUND, filePath));
        }
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
    }

    private String getActualValue(final Assert aAssert, final CommandResult result) {
        result.put(ASSERT_LOCATOR, aAssert.getLocatorId());
        result.put(ASSERT_ATTRIBUTE, aAssert.getAttribute().value());
        WebElement webElement = getWebElement(aAssert.getLocatorId());
        ExplicitWaitUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
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
        ExplicitWaitUtil.waitForElementVisibility(dependencies.getWebDriver(), element);
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
        Vertical vertical = scroll.getVertical();
        Horizontal horizontal = scroll.getHorizontal();
        ToElement toElement = scroll.getToElement();
        VerticalAndHorizontal verticalAndHorizontal = scroll.getVerticalAndHorizontal();
        if (vertical != null) {
            log.info(SCROLL_TYPE_LOG, vertical.getClass().getSimpleName());
            scrollVertical(vertical, result);
        } else if (horizontal != null) {
            result.put(SCROLL_ACTION, format(SCROLL_TO_DIRECTION_INFO, horizontal.getClass().getSimpleName(),
                    horizontal.getDirection().value(),
                    horizontal.getBy().value(), horizontal.getValue()));
            log.info(SCROLL_TYPE_LOG, horizontal.getClass().getSimpleName());
            scrollHorizontal(horizontal, result);
        } else if (toElement != null) {
            result.put(SCROLL_ACTION, format(SCROLL_TO_ELEMENT_INFO, toElement.getClass().getSimpleName(),
                    toElement.getBy().value(), toElement.getValue()));
            log.info(SCROLL_TYPE_LOG, toElement.getClass().getSimpleName());
            scrollToElement(toElement.getBy(), result, toElement.getValue());
        } else {
            log.info(SCROLL_TYPE_LOG, verticalAndHorizontal.getClass().getSimpleName());
            scrollVerticalAndHorizontal(verticalAndHorizontal, result);
        }
    }

    private void scrollToElement(final ScrollToElementBy by, final CommandResult result, final String value) {
        WebDriver driver = dependencies.getWebDriver();
        WebElement element = null;
        log.info(SCROLL_BY_LOG, by.name());
        log.info(VALUE_LOG, value);
        switch (by) {
            case BY_ID: element = getWebElement(value);
                break;
            case BY_TEXT: element = driver.findElement(By.linkText(value));
                break;
            case BY_XPATH: element = driver.findElement(By.xpath(value));
            break;
            default: throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, by.value()));
        }
        executeJsScript(element, result, SCROLL_TO_ELEMENT_SCRIPT);
    }

    private void scrollHorizontal(final Horizontal horizontal, final CommandResult result) {
        executeJsScriptWithoutWebEl(result, format(SCROLL_HORIZONTAL_SCRIPT_FORMAT,
                scrollHorizontalByValue(horizontal.getDirection(), horizontal.getValue(), horizontal.getBy())));
    }

    private void scrollVertical(final Vertical vertical, final CommandResult result) {
        VerticalByValue verticalByValue = vertical.getScrollByValue();
        if (verticalByValue != null) {
            result.put(SCROLL_ACTION, format(SCROLL_TO_DIRECTION_INFO, vertical.getClass().getSimpleName(),
                    verticalByValue.getDirection().value(), verticalByValue.getBy().value(),
                    verticalByValue.getValue()));
            executeJsScriptWithoutWebEl(result, format(SCROLL_VERTICAL_SCRIPT_FORMAT,
                    scrollVerticalByValue(verticalByValue.getDirection(),
                            verticalByValue.getValue(), verticalByValue.getBy())));
        } else {
            result.put(SCROLL_ACTION, format(SCROLL_TOP_OR_BOTTOM_INFO, vertical.getClass().getSimpleName(),
                    vertical.getScrollToTopOrBottom().value()));
            scrollToTopOrBottom(result, vertical.getScrollToTopOrBottom());
        }
    }

    private void scrollVerticalAndHorizontal(final VerticalAndHorizontal verticalAndHorizontal,
                                             final CommandResult result) {
        result.put(SCROLL_ACTION, format(SCROLL_V_AND_H_INFO, verticalAndHorizontal.getClass().getSimpleName(),
                verticalAndHorizontal.getBy().value(), verticalAndHorizontal.getHDirection().value(),
                verticalAndHorizontal.getHorizontalValue(), verticalAndHorizontal.getVDirection().value(),
                verticalAndHorizontal.getVerticalValue()));
        String horizontal = scrollHorizontalByValue(verticalAndHorizontal.getHDirection(),
                verticalAndHorizontal.getHorizontalValue(), verticalAndHorizontal.getBy());
        String vertical = scrollVerticalByValue(verticalAndHorizontal.getVDirection(),
                verticalAndHorizontal.getVerticalValue(), verticalAndHorizontal.getBy());
        executeJsScriptWithoutWebEl(result, format(SCROLL_VERTICAL_AND_HORIZONTAL_FORMAT, horizontal, vertical));
    }

    private String scrollHorizontalByValue(final ScrollHorizontalDir horizontal, final String value,
                                           final ScrollBy by) {
        log.info(SCROLL_DIRECTION_LOG, horizontal.name());
        log.info(VALUE_LOG, value);
        if (horizontal.equals(ScrollHorizontalDir.LEFT)) {
            return scrollBy(by, "-" + value, SCROLL_HORIZONTAL_PERCENT);
        }
        return scrollBy(by, value, SCROLL_HORIZONTAL_PERCENT);
    }

    private String scrollVerticalByValue(final ScrollVerticalDir vertical, final String value, final ScrollBy by) {
        log.info(SCROLL_DIRECTION_LOG, vertical.name());
        log.info(VALUE_LOG, value);
        if (vertical.equals(ScrollVerticalDir.UP)) {
            return scrollBy(by, "-" + value, SCROLL_VERTICAL_PERCENT);
        }
        return scrollBy(by, value, SCROLL_VERTICAL_PERCENT);
    }

    private void scrollToTopOrBottom(final CommandResult result, final ScrollToTopOrBottom topOrBottom) {
        log.info(SCROLL_DIRECTION_LOG, topOrBottom.name());
        switch (topOrBottom) {
            case TO_THE_TOP:
                executeJsScriptWithoutWebEl(result, SCROLL_TO_TOP_SCRIPT);
                break;
            case TO_THE_BOTTOM:
                executeJsScriptWithoutWebEl(result, SCROLL_TO_BOTTOM_SCRIPT);
                break;
            default: throw new DefaultFrameworkException(format(SCROLL_TO_ELEMENT_NOT_SUPPORTED, topOrBottom.value()));
        }
    }

    private String scrollBy(final ScrollBy by, final String value, final String dirPercentScript) {
        log.info(SCROLL_BY_LOG, by.name());
        if (by.equals(ScrollBy.BY_PERCENT)) {
            float percent = Float.parseFloat(value) / percentage;
            return format(dirPercentScript, percent);
        }
        return value;
    }


    private interface UiCommandPredicate extends Predicate<AbstractCommand> { }
    private interface UiCommand extends BiConsumer<AbstractCommand, CommandResult> { }
}
