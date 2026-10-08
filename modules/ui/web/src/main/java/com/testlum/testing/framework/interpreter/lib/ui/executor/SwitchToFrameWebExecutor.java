package com.testlum.testing.framework.interpreter.lib.ui.executor;

import com.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.testlum.testing.framework.report.CommandResult;
import com.testlum.testing.framework.util.ResultUtil;
import com.testlum.testing.framework.util.check.ElementChecks;
import com.testlum.testing.model.scenario.SwitchToFrame;
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
        screenshotUtil.takeScreenshotAndSaveIfRequired(result, dependencies);

        uiLogUtil.startUiCommandsInFrame();
        this.subCommandRunner.runCommands(switchToFrame.getClickOrInputOrAssert(), result, dependencies);
        uiLogUtil.endUiCommandsInFrame();
        dependencies.getDriver().switchTo().parentFrame();
    }

    private void switchToFrameByLocator(final SwitchToFrame switchToFrame, final CommandResult result,
                                        final String locatorId) {
        result.put(ResultUtil.SWITCH_LOCATOR, locatorId);
        WebElement element = uiUtil.findWebElement(dependencies, locatorId, switchToFrame.getLocatorStrategy(),
                ElementChecks.FOR_READING, result);
        dependencies.getDriver().switchTo().frame(element);
    }

    private void switchToFrameByIndex(final SwitchToFrame switchToFrame, final CommandResult result) {
        result.put(ResultUtil.SWITCH_INDEX, switchToFrame.getIndex());
        dependencies.getDriver().switchTo().frame(Integer.parseInt(switchToFrame.getIndex()));
    }
}
