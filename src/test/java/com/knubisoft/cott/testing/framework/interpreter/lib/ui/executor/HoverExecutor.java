package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Hovers;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;

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
        hovers.getHover().stream()
                .peek(hover -> LogUtil.logHover(dependencies.getPosition().incrementAndGet(), hover))
                .map(hover -> UiUtil.findWebElement(dependencies.getDriver(), hover.getLocatorId()))
                .forEach(webElement -> performMovement(actions, webElement));
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
        moveToEmptySpace(hovers.isMoveToEmptySpace(), actions);
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
