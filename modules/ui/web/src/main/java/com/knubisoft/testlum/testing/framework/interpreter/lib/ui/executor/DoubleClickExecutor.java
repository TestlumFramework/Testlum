package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.DoubleClick;
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
        WebElement webElement = uiUtil.findWebElement(dependencies, click.getLocator(), click.getLocatorStrategy());
        uiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        Actions act = new Actions(dependencies.getDriver());
        act.doubleClick(webElement).perform();
    }

}

