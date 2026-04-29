package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.SwitchToFrame;
import org.openqa.selenium.WebElement;

@ExecutorForClass(SwitchToFrame.class)
public class SwitchToFrameWebExecutor extends AbstractUiExecutor<SwitchToFrame> {

    private final SubCommandRunnerImpl subCommandRunner;

    public SwitchToFrameWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
        this.subCommandRunner = dependencies.getContext().getBean(SubCommandRunnerImpl.class);
    }

    @Override
    public void execute(final SwitchToFrame switchToFrame, final CommandResult result) {
        String locatorId = switchToFrame.getLocator();
        if (locatorId != null) {
            switchToFrameByLocator(switchToFrame, result, locatorId);
        } else {
            switchToFrameByIndex(switchToFrame, result);
        }
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);

        uiLogUtil.startUiCommandsInFrame();
        this.subCommandRunner.runCommands(switchToFrame.getClickOrInputOrAssert(), result, dependencies);
        uiLogUtil.endUiCommandsInFrame();
        dependencies.getDriver().switchTo().parentFrame();
    }

    private void switchToFrameByLocator(final SwitchToFrame switchToFrame, final CommandResult result,
                                        final String locatorId) {
        result.put(ResultUtil.SWITCH_LOCATOR, locatorId);
        WebElement element = uiUtil.findWebElement(dependencies, locatorId, switchToFrame.getLocatorStrategy());
        dependencies.getDriver().switchTo().frame(element);
    }

    private void switchToFrameByIndex(final SwitchToFrame switchToFrame, final CommandResult result) {
        result.put(ResultUtil.SWITCH_INDEX, switchToFrame.getIndex());
        dependencies.getDriver().switchTo().frame(Integer.parseInt(switchToFrame.getIndex()));
    }
}
