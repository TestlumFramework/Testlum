package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import static com.knubisoft.testlum.testing.framework.util.ConditionProviderImpl.ConditionUtil;

@ExecutorForClass(Hover.class)
public class HoverExecutor extends AbstractUiExecutor<Hover> {

    private static final String MOVE_TO_EMPTY_SPACE = "//html";

    public HoverExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Hover hover, final CommandResult result) {
        ResultUtil.addHoverMetaData(hover, result);
        LogUtil.logHover(hover);
        Actions actions = new Actions(dependencies.getDriver());
        if (ConditionUtil.isTrue(hover.getCondition(), dependencies.getScenarioContext(), result)) {
            executeHoverCommand(actions, hover);
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        moveToEmptySpace(hover.isMoveToEmptySpace(), actions);
    }

    private void executeHoverCommand(final Actions actions, final Hover hover) {
        WebElement webElement = UiUtil.findWebElement(dependencies, hover.getLocatorId(), hover.getLocatorStrategy());
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
