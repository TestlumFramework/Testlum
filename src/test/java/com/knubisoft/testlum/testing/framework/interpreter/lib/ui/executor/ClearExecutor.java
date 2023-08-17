package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;

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
        String locatorId = clear.getLocatorId();
        result.put(CLEAR_LOCATOR, locatorId);
        WebElement element = uiUtil.findWebElement(dependencies, locatorId);
        uiUtil.waitForElementVisibility(dependencies, element);
        uiUtil.highlightElementIfRequired(clear.isHighlight(), element, dependencies.getDriver());
        element.clear();
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
