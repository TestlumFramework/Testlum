package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.check.EnabledCheck;
import com.knubisoft.testlum.testing.framework.util.check.InteractabilityCheck;
import com.knubisoft.testlum.testing.framework.util.check.VisibilityCheck;
import com.knubisoft.testlum.testing.model.scenario.Input;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

@Slf4j
@ExecutorForClass(Input.class)
public class InputExecutor extends AbstractUiExecutor<Input> {

    public InputExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Input input, final CommandResult result) {
        result.put(ResultUtil.INPUT_LOCATOR, input.getLocator());
        WebElement webElement = uiUtil.findWebElement(dependencies, input.getLocator(), input.getLocatorStrategy(),
                new VisibilityCheck(), new InteractabilityCheck(), new EnabledCheck());
        uiUtil.highlightElementIfRequired(input.isHighlight(), webElement, dependencies.getDriver());
        String value = uiUtil.resolveSendKeysType(input.getValue(), webElement, dependencies.getFile());
        result.put(ResultUtil.INPUT_VALUE, value);
        log.info(LogMessage.VALUE_LOG, value);
        webElement.sendKeys(value);
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
