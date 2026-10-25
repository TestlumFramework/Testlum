package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.Hover;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

@ExecutorForClass(Hover.class)
public class HoverExecutor extends AbstractUiExecutor<Hover> {

    private static final String MOVE_TO_EMPTY_SPACE = "//html";

    public HoverExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Hover hover, final CommandResult result) {
        resultUtil.addHoverMetaData(hover, result);
        uiLogUtil.logHover(hover);
        Actions actions = new Actions(dependencies.getDriver());
        if (conditionUtil.isTrue(hover.getCondition(), dependencies.getScenarioContext(), result)) {
            executeHoverCommand(actions, hover, result);
        }
        screenshotUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        moveToEmptySpace(hover.isMoveToEmptySpace(), actions);
    }

    private void executeHoverCommand(final Actions actions, final Hover hover, final CommandResult result) {
        WebElement webElement = uiUtil.findWebElement(dependencies, hover.getLocator(), hover.getLocatorStrategy(),
                ElementChecks.FOR_READING, result);
        performMovement(actions, webElement);
    }

    private void moveToEmptySpace(final boolean isMoveToEmptySpace, final Actions actions) {
        if (isMoveToEmptySpace) {
            WebElement element = dependencies.getDriver().findElement(By.xpath(MOVE_TO_EMPTY_SPACE));
            performMovement(actions, element);
        }
    }

    private void performMovement(final Actions actions, final WebElement webElement) {
        actions.moveToElement(webElement);
        actions.perform();
    }
}
