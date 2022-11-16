package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.JavascriptUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Click;
import com.knubisoft.cott.testing.model.scenario.ClickMethod;
import org.openqa.selenium.WebElement;

import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLICK_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLICK_METHOD;
import static com.knubisoft.cott.testing.model.scenario.ClickMethod.JS;

@ExecutorForClass(Click.class)
public class ClickExecutor extends AbstractUiExecutor<Click> {

    public ClickExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Click click, final CommandResult result) {
        result.put(CLICK_LOCATOR, click.getLocatorId());
        WebElement webElement = UiUtil.findWebElement(dependencies.getDriver(), click.getLocatorId());
        UiUtil.waitForElementVisibility(dependencies.getDriver(), webElement);
        UiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (method != null && method.equals(JS)) {
            result.put(CLICK_METHOD, "javascript");
            JavascriptUtil.executeJsScript(element, CLICK_SCRIPT, dependencies.getDriver());
        } else {
            result.put(CLICK_METHOD, "selenium");
            element.click();
        }
    }
}
