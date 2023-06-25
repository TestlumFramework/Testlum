package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.Hover;
import com.knubisoft.testlum.testing.model.scenario.Hovers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

import java.util.ArrayList;
import java.util.List;

@ExecutorForClass(Hovers.class)
public class HoverExecutor extends AbstractUiExecutor<Hovers> {

    private static final String MOVE_TO_EMPTY_SPACE = "//html";

    public HoverExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Hovers hovers, final CommandResult result) {
        ResultUtil.addHoversMetaData(hovers, result);
        Actions actions = new Actions(dependencies.getDriver());
        executeSubCommands(hovers, result, actions);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        moveToEmptySpace(hovers.isMoveToEmptySpace(), actions);
    }

    private void executeSubCommands(final Hovers hovers,
                                    final CommandResult result,
                                    final Actions actions) {
        List<CommandResult> subCommandsResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandsResult);
        hovers.getHover().forEach(hover -> {
            CommandResult commandResult =
                    ResultUtil.newUiCommandResultInstance(dependencies.getPosition().incrementAndGet(), hover);
            subCommandsResult.add(commandResult);
            if (ConditionUtil.isTrue(hover.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                executeHoverCommand(actions, hover, commandResult);
            }
        });
    }

    private void executeHoverCommand(final Actions actions,
                                     final Hover hover,
                                     final CommandResult commandResult) {
        LogUtil.logHover(dependencies.getPosition().get(), hover);
        ResultUtil.addHoverMetaData(hover.getComment(), hover.getLocatorId(), commandResult);
        WebElement webElement = UiUtil.findWebElement(dependencies, hover.getLocatorId());
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
