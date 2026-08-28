package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.constant.JavascriptConstant;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.model.scenario.ScrollTo;
import org.openqa.selenium.WebElement;

@ExecutorForClass(ScrollTo.class)
public class ScrollToWebExecutor extends AbstractUiExecutor<ScrollTo> {

    public ScrollToWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final ScrollTo scrollTo, final CommandResult result) {
        String locatorId = scrollTo.getLocator();
        WebElement element = uiUtil.findWebElement(dependencies, locatorId, scrollTo.getLocatorStrategy());
        result.put(ResultUtil.SCROLL_LOCATOR, locatorId);
        javascriptUtil.executeJsScript(JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT, dependencies.getDriver(), element);
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
