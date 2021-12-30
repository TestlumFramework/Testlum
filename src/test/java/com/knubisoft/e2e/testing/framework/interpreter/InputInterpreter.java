package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.Input;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.framework.util.SeleniumUtil;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

@Slf4j
@InterpreterForClass(Input.class)
public class InputInterpreter extends AbstractSeleniumInterpreter<Input> {

    public InputInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Input o, final CommandResult result) {
        result.setLocatorId(o.getLocatorId());
        WebElement element = getWebElement(o.getLocatorId());
        highlightElementIfRequired(o.isHighlight(), element);
        send(o, element, result);
        takeScreenshotIfRequired(result);
    }

    private void send(final Input o, final WebElement element, final CommandResult result) {
        String injected = inject(o.getValue());
        String text = SeleniumUtil.resolveSendKeysType(injected,
                dependencies.getFileSearcher(), element);
        result.put("value", text);
        log.info(text);
        element.sendKeys(text);
    }
}
