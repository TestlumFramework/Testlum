package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.SwitchToFrame;
import org.openqa.selenium.WebElement;

import static com.knubisoft.cott.testing.framework.util.ResultUtil.SWITCH_LOCATOR;

@ExecutorForClass(SwitchToFrame.class)
public class SwitchToFrameWebExecutor extends AbstractUiExecutor<SwitchToFrame> {

    public SwitchToFrameWebExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final SwitchToFrame switchToFrame, final CommandResult result) {
        String locatorId = switchToFrame.getLocatorId();
        result.put(SWITCH_LOCATOR, locatorId);
        WebElement element = UiUtil.findWebElement(dependencies.getDriver(), locatorId);
        dependencies.getDriver().switchTo().frame(element);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
