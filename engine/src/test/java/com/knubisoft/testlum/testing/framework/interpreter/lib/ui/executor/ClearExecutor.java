package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Clear;
import org.openqa.selenium.WebElement;

import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CLEAR_LOCATOR;

@ExecutorForClass(Clear.class)
public class ClearExecutor extends AbstractUiExecutor<Clear> {

    public ClearExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Clear clear, final CommandResult result) {
        String locatorId = clear.getLocator();
        result.put(CLEAR_LOCATOR, locatorId);
        WebElement element = UiUtil.findWebElement(dependencies, locatorId, clear.getLocatorStrategy());
        UiUtil.waitForElementVisibility(dependencies, element);
        UiUtil.highlightElementIfRequired(clear.isHighlight(), element, dependencies.getDriver());
        element.clear();
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
