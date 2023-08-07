package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Click;
import com.knubisoft.testlum.testing.model.scenario.ClickMethod;
import org.openqa.selenium.WebElement;

import static com.knubisoft.testlum.testing.framework.constant.JavascriptConstant.CLICK_SCRIPT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLICK_LOCATOR;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLICK_METHOD;

@ExecutorForClass(Click.class)
public class ClickExecutor extends AbstractUiExecutor<Click> {

    public ClickExecutor(final GlobalTestConfigurationProvider configurationProvider,
                         final ExecutorDependencies dependencies) {
        super(configurationProvider, dependencies);
    }

    @Override
    public void execute(final Click click, final CommandResult result) {
        result.put(CLICK_LOCATOR, click.getLocatorId());
        WebElement webElement = UiUtil.findWebElement(dependencies, click.getLocatorId());
//        UiUtil.waitForElementVisibility(dependencies, webElement);
        UiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        clickWithMethod(click.getMethod(), webElement, result);
    }

    private void clickWithMethod(final ClickMethod method, final WebElement element, final CommandResult result) {
        if (ClickMethod.JS == method) {
            result.put(CLICK_METHOD, "javascript");
            JavascriptUtil.executeJsScript(CLICK_SCRIPT, dependencies.getDriver(), element);
        } else {
            result.put(CLICK_METHOD, "selenium");
            element.click();
        }
    }
}
