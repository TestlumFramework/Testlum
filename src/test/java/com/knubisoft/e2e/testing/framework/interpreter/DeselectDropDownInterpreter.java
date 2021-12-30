package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.DeselectDropDown;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@InterpreterForClass(DeselectDropDown.class)
public class DeselectDropDownInterpreter extends AbstractSeleniumInterpreter<DeselectDropDown> {

    public DeselectDropDownInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final DeselectDropDown o, final CommandResult result) {
        result.setLocatorId(o.getLocatorId());
        WebElement element = getWebElement(o.getLocatorId());
        Select select = new Select(element);
        deselectByMethod(o, select, result);
        takeScreenshotIfRequired(result);
    }

    //CHECKSTYLE:OFF
    private void deselectByMethod(final DeselectDropDown o, final Select select, final CommandResult result) {
        if (o.getIndex() != null) {
            result.put("type", "deselect by index");
            select.deselectByIndex(o.getIndex());
        } else if (o.getText() != null) {
            result.put("type", "deselect by visible text");
            select.deselectByVisibleText(inject(o.getText()));
        } else {
            result.put("type", "deselect by value");
            select.deselectByValue(inject(o.getValue()));
        }
    }
    //CHECKSTYLE:ON
}
