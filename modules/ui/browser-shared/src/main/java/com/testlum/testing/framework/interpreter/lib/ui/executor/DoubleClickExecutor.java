package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.DoubleClick;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@ExecutorForClass(DoubleClick.class)
public class DoubleClickExecutor extends AbstractUiExecutor<DoubleClick> {

    public DoubleClickExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final DoubleClick click, final CommandResult result) {
        result.put(ResultUtil.DOUBLE_CLICK_LOCATOR, click.getLocator());
        WebElement webElement = uiUtil.findWebElement(dependencies, click.getLocator(), click.getLocatorStrategy(),
                ElementChecks.FOR_INTERACTION, result);
        uiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        screenshotUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        Actions act = new Actions(dependencies.getDriver());
        act.doubleClick(webElement).perform();
    }

}

