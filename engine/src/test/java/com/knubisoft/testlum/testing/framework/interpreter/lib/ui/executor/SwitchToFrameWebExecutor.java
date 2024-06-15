package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.interpreter.lib.SubCommandRunnerImpl;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.LogUtil;

import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.SwitchToFrame;
import org.openqa.selenium.WebElement;
import org.springframework.beans.factory.annotation.Autowired;

import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SWITCH_INDEX;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.SWITCH_LOCATOR;

@ExecutorForClass(SwitchToFrame.class)
public class SwitchToFrameWebExecutor extends AbstractUiExecutor<SwitchToFrame> {

    @Autowired
    private SubCommandRunnerImpl subCommandRunner;

    public SwitchToFrameWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final SwitchToFrame switchToFrame, final CommandResult result) {
        String locatorId = switchToFrame.getLocator();
        if (locatorId != null) {
            result.put(SWITCH_LOCATOR, locatorId);
            WebElement element = UiUtil.findWebElement(dependencies, locatorId, switchToFrame.getLocatorStrategy());
            dependencies.getDriver().switchTo().frame(element);
        } else {
            result.put(SWITCH_INDEX, switchToFrame.getIndex());
            dependencies.getDriver().switchTo().frame(switchToFrame.getIndex());
        }
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);

        LogUtil.startUiCommandsInFrame();
        this.subCommandRunner.runCommands(switchToFrame.getClickOrInputOrAssert(), result, dependencies);
        LogUtil.endUiCommandsInFrame();
        dependencies.getDriver().switchTo().parentFrame();
    }
}
