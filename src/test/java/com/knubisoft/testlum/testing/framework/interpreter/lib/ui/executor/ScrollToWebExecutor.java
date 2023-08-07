package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.JavascriptUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.ScrollTo;
import org.openqa.selenium.WebElement;

import static com.knubisoft.testlum.testing.framework.constant.JavascriptConstant.SCROLL_TO_ELEMENT_SCRIPT;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SCROLL_LOCATOR;

@ExecutorForClass(ScrollTo.class)
public class ScrollToWebExecutor extends AbstractUiExecutor<ScrollTo> {

    public ScrollToWebExecutor(final GlobalTestConfigurationProvider configurationProvider,
                               final ExecutorDependencies dependencies) {
        super(configurationProvider, dependencies);
    }

    @Override
    public void execute(final ScrollTo scrollTo, final CommandResult result) {
        String locatorId = scrollTo.getLocatorId();
        WebElement element = UiUtil.findWebElement(dependencies, locatorId);
        result.put(SCROLL_LOCATOR, locatorId);
        JavascriptUtil.executeJsScript(SCROLL_TO_ELEMENT_SCRIPT, dependencies.getDriver(), element);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
