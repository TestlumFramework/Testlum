package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.ExplicitWaitUtil;
import com.knubisoft.e2e.testing.framework.util.SeleniumUtil;
import com.knubisoft.e2e.testing.model.scenario.Assert;
import com.knubisoft.e2e.testing.model.scenario.Click;
import com.knubisoft.e2e.testing.model.scenario.ClickMethod;
import com.knubisoft.e2e.testing.model.scenario.DeselectDropDown;
import com.knubisoft.e2e.testing.model.scenario.DeselectDropDownAll;
import com.knubisoft.e2e.testing.model.scenario.Input;
import com.knubisoft.e2e.testing.model.scenario.NavigateBack;
import com.knubisoft.e2e.testing.model.scenario.NavigateReload;
import com.knubisoft.e2e.testing.model.scenario.NavigateTo;
import com.knubisoft.e2e.testing.model.scenario.SelectDropDown;
import com.knubisoft.e2e.testing.model.scenario.Ui;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.BY_URL_LOG;
import static com.knubisoft.e2e.testing.model.scenario.ClickMethod.JS;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.SPACE;


@Slf4j
@InterpreterForClass(Ui.class)
public class UiInterpreter extends AbstractSeleniumInterpreter<Ui> {

    public UiInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    //TODO refactoring
    //CHECKSTYLE:OFF
    @Override
    protected void acceptImpl(final Ui o, final CommandResult result) {
        for (Object action : o.getClickOrInputOrNavigateBack()) {
            if(action instanceof Click) {
                click((Click) action, result);
            } else if (action instanceof Input) {
                input((Input) action, result);
            } else if (action instanceof NavigateBack) {
                navigateBack();
            } else if (action instanceof NavigateReload) {
                navigateReload();
            } else if (action instanceof NavigateTo) {
                navigateTo(((NavigateTo) action).getPath());
            } else if (action instanceof Assert) {
                assertValues((Assert) action, result);
            } else if (action instanceof SelectDropDown) {
                selectDropDown((SelectDropDown) action, result);
            } else if (action instanceof DeselectDropDown) {
                deselectDropDown((DeselectDropDown) action, result);
            } else if (action instanceof DeselectDropDownAll) {
                deselectDropDownAll((DeselectDropDownAll) action, result);
            }
        }
    }
    //CHECKSTYLE:ON

    private void click(final Click click, final CommandResult result) {
        result.setLocatorId(click.getLocatorId());
        WebElement webElement = getWebElement(click.getLocatorId());
        ExplicitWaitUtil.waitForElementVisibility(dependencies.getWebDriver(), webElement);
        highlightElementIfRequired(click.isHighlight(), webElement);
        takeScreenshotIfRequired(result);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put("method", "js");
            executeJsScript(element, result);
        } else {
            result.put("method", "selenium");
            element.click();
        }
    }

    private void executeJsScript(final WebElement element, final CommandResult result) {
        JavascriptExecutor js = (JavascriptExecutor) dependencies.getWebDriver();
        takeScreenshotIfRequired(result);
        js.executeScript(CLICK_SCRIPT, element);
    }

    private void input(final Input input, final CommandResult result) {
        result.setLocatorId(input.getLocatorId());
        WebElement webElement = getWebElement(input.getLocatorId());
        highlightElementIfRequired(input.isHighlight(), webElement);
        send(input, webElement, result);
        takeScreenshotIfRequired(result);
    }

    private void send(final Input input, final WebElement element, final CommandResult result) {
        String injected = inject(input.getValue());
        String text = SeleniumUtil.resolveSendKeysType(injected, dependencies.getFileSearcher(), element);
        result.put("value", text);
        log.info(text);
        element.sendKeys(text);
    }

    private void navigateBack() {
        dependencies.getWebDriver().navigate().back();
    }

    private void navigateReload() {
        dependencies.getWebDriver().navigate().refresh();
    }

    private void navigateTo(final String path) {
        String url = inject(dependencies.getGlobalTestConfiguration().getUiConfiguration().getBaseUrl() + path);
        log.info(BY_URL_LOG, url);
        dependencies.getWebDriver().navigate().to(url);
    }

    private void assertValues(final Assert aAssert, final CommandResult result) {
        String actual = getActualValue(aAssert, result);
        String expected = getExpectedValue(aAssert);
        result.setActual(actual);
        result.setExpected(expected);
        newCompare()
                .withActual(actual)
                .withExpected(expected)
                .exec();
    }

    private String getActualValue(final Assert aAssert, final CommandResult result) {
        result.setLocatorId(aAssert.getLocatorId());
        WebElement webElement = getWebElement(aAssert.getLocatorId());
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

    private void selectDropDown(final SelectDropDown selectDropDown, final CommandResult result) {
        result.setLocatorId(selectDropDown.getLocatorId());
        WebElement webElement = getWebElement(selectDropDown.getLocatorId());
        Select select = new Select(webElement);
        selectByMethod(selectDropDown, select, result);
        takeScreenshotIfRequired(result);
    }

    //TODO refactoring
    //CHECKSTYLE:OFF
    private void selectByMethod(final SelectDropDown selectDropDown, final Select select, final CommandResult result) {
        if(selectDropDown.getIndex() != null) {
            result.put("type", "select by index");
            select.selectByIndex(selectDropDown.getIndex());
        } else if (selectDropDown.getText() != null) {
            result.put("type", "select by visible text");
            select.selectByVisibleText(inject(selectDropDown.getText()));
        } else {
            result.put("type", "select by value");
            select.selectByValue(inject(selectDropDown.getValue()));
        }
    }
    //CHECKSTYLE:ON

    private void deselectDropDown(final DeselectDropDown deselectDropDown, final CommandResult result) {
        result.setLocatorId(deselectDropDown.getLocatorId());
        WebElement webElement = getWebElement(deselectDropDown.getLocatorId());
        Select select = new Select(webElement);
        deselectByMethod(deselectDropDown, select, result);
        takeScreenshotIfRequired(result);
    }

    //TODO refactoring
    //CHECKSTYLE:OFF
    private void deselectByMethod(final DeselectDropDown deselectDropDown,
                                  final Select select,
                                  final CommandResult result) {
        if (deselectDropDown.getIndex() != null) {
            result.put("type", "deselect by index");
            select.deselectByIndex(deselectDropDown.getIndex());
        } else if (deselectDropDown.getText() != null) {
            result.put("type", "deselect by visible text");
            select.deselectByVisibleText(inject(deselectDropDown.getText()));
        } else {
            result.put("type", "deselect by value");
            select.deselectByValue(inject(deselectDropDown.getValue()));
        }
    }
    //CHECKSTYLE:ON

    private void deselectDropDownAll(final DeselectDropDownAll deselectDropDownAll, final CommandResult result) {
        result.setLocatorId(deselectDropDownAll.getLocatorId());
        WebElement webElement = getWebElement(deselectDropDownAll.getLocatorId());
        Select select = new Select(webElement);
        select.deselectAll();
    }
}
