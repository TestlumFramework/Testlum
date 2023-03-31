package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.JavascriptUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.ScrollTo;
import org.openqa.selenium.WebElement;

import static com.knubisoft.cott.testing.framework.constant.JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.SCROLL_LOCATOR;

@ExecutorForClass(ScrollTo.class)
public class ScrollToWebExecutor extends AbstractUiExecutor<ScrollTo> {

    public ScrollToWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final ScrollTo scrollTo, final CommandResult result) {
        String locatorId = scrollTo.getLocatorId();
        WebElement element = UiUtil.findWebElement(dependencies, locatorId);
        result.put(SCROLL_LOCATOR, locatorId);
        JavascriptUtil.executeJsScript(element, SCROLL_TO_ELEMENT_SCRIPT, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
