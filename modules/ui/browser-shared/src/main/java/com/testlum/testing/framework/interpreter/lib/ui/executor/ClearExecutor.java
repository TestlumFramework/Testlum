package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.Clear;
import org.openqa.selenium.WebElement;

@ExecutorForClass(Clear.class)
public class ClearExecutor extends AbstractUiExecutor<Clear> {

    public ClearExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Clear clear, final CommandResult result) {
        String locatorId = clear.getLocator();
        result.put(ResultUtil.CLEAR_LOCATOR, locatorId);
        WebElement element = uiUtil.findWebElement(dependencies, locatorId, clear.getLocatorStrategy(),
                ElementChecks.FOR_WRITING, result);
        uiUtil.highlightElementIfRequired(clear.isHighlight(), element, dependencies.getDriver());
        element.clear();
        screenshotUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
