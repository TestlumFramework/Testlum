package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.DoubleClick;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.DOUBLE_CLICK_LOCATOR;

@ExecutorForClass(DoubleClick.class)
public class DoubleClickExecutor extends AbstractUiExecutor<DoubleClick> {

    public DoubleClickExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final DoubleClick click, final CommandResult result) {
        result.put(DOUBLE_CLICK_LOCATOR, click.getLocator());
        WebElement webElement = UiUtil.findWebElement(dependencies, click.getLocator(), click.getLocatorStrategy());
        UiUtil.highlightElementIfRequired(click.isHighlight(), webElement, dependencies.getDriver());
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        Actions act = new Actions(dependencies.getDriver());
        act.doubleClick(webElement).perform();
    }

}

