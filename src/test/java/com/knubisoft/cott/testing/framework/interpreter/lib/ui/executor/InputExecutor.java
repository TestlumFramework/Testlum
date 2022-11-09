package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Input;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import static com.knubisoft.cott.testing.framework.constant.LogMessage.VALUE_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.INPUT_LOCATOR;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.INPUT_VALUE;

@Slf4j
@ExecutorForClass(Input.class)
public class InputExecutor extends AbstractUiExecutor<Input> {

    public InputExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Input input, final CommandResult result) {
        result.put(INPUT_LOCATOR, input.getLocatorId());
        WebElement webElement = UiUtil.findWebElement(dependencies.getDriver(), input.getLocatorId());
        UiUtil.highlightElementIfRequired(input.isHighlight(), webElement, dependencies.getDriver());
        String injected = inject(input.getValue());
        String value = UiUtil.resolveSendKeysType(injected, webElement, dependencies.getFile());
        result.put(INPUT_VALUE, value);
        log.info(VALUE_LOG, value);
        webElement.sendKeys(value);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }
}
