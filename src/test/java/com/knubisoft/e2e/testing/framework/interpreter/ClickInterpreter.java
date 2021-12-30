package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.Click;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.ExplicitWaitUtil;
import com.knubisoft.e2e.testing.model.scenario.ClickMethod;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;

import static com.knubisoft.e2e.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.e2e.testing.model.scenario.ClickMethod.JS;

@Slf4j
@InterpreterForClass(Click.class)
public class ClickInterpreter extends AbstractSeleniumInterpreter<Click> {

    public ClickInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Click o, final CommandResult result) {
        result.setLocatorId(o.getLocatorId());
        WebElement element = getWebElement(o.getLocatorId());
        ExplicitWaitUtil.waitForElementVisibility(dependencies.getWebDriver(), element);
        highlightElementIfRequired(o.isHighlight(), element);
        takeScreenshotIfRequired(result);
        clickWithMethod(o.getMethod(), element, result);
    }

    //CHECKSTYLE:OFF
    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put("method", "js");
            JavascriptExecutor js = (JavascriptExecutor) dependencies.getWebDriver();
            takeScreenshotIfRequired(result);
            js.executeScript(CLICK_SCRIPT, element);
        } else {
            result.put("method", "selenium");
            element.click();
        }
    }
    //CHECKSTYLE:ON
}
