package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.JavascriptConstant;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.check.EnabledCheck;
import com.knubisoft.testlum.testing.framework.util.check.InteractabilityCheck;
import com.knubisoft.testlum.testing.framework.util.check.VisibilityCheck;
import com.knubisoft.testlum.testing.model.scenario.Click;
import com.knubisoft.testlum.testing.model.scenario.ClickMethod;
import org.openqa.selenium.WebElement;

@ExecutorForClass(Click.class)
public class ClickExecutor extends AbstractUiExecutor<Click> {

    public ClickExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Click click, final CommandResult result) {
        result.put(ResultUtil.CLICK_LOCATOR, click.getLocator());
        WebElement webElement = uiUtil.findWebElement(dependencies, click.getLocator(), click.getLocatorStrategy(),
                new VisibilityCheck(), new InteractabilityCheck(), new EnabledCheck());
        uiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (ClickMethod.JS == method) {
            result.put(ResultUtil.CLICK_METHOD, "javascript");
            javascriptUtil.executeJsScript(JavascriptConstant.CLICK_SCRIPT, dependencies.getDriver(), element);
        } else {
            result.put(ResultUtil.CLICK_METHOD, "selenium");
            element.click();
        }
    }
}
