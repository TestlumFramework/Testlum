package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Clear;
import org.openqa.selenium.WebElement;

import static com.knubisoft.cott.testing.framework.util.ResultUtil.CLEAR_LOCATOR;

@ExecutorForClass(Clear.class)
public class ClearExecutor extends AbstractUiExecutor<Clear> {

    public ClearExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Clear clear, final CommandResult result) {
        String locatorId = clear.getLocatorId();
        result.put(CLEAR_LOCATOR, locatorId);
        UiUtil.waitForElementVisibility(dependencies.getDriver(), locatorId);
        WebElement element = UiUtil.findWebElement(dependencies.getDriver(), locatorId);
        UiUtil.highlightElementIfRequired(clear.isHighlight(), element, dependencies.getDriver());
        element.clear();
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
