package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.DeselectDropDownAll;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@InterpreterForClass(DeselectDropDownAll.class)
public class DeselectDropDownAllInterpreter extends AbstractSeleniumInterpreter<DeselectDropDownAll> {

    public DeselectDropDownAllInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final DeselectDropDownAll o, final CommandResult result) {
        result.setLocatorId(o.getLocatorId());
        WebElement element = getWebElement(o.getLocatorId());
        Select select = new Select(element);
        select.deselectAll();
    }
}
