package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.SelectDropDown;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;

@InterpreterForClass(SelectDropDown.class)
public class SelectDropDownInterpreter extends AbstractSeleniumInterpreter<SelectDropDown> {

    public SelectDropDownInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final SelectDropDown o, final CommandResult result) {
        result.setLocatorId(o.getLocatorId());
        WebElement element = getWebElement(o.getLocatorId());
        Select select = new Select(element);
        selectByMethod(o, select, result);
        takeScreenshotIfRequired(result);
    }

    //CHECKSTYLE:OFF
    private void selectByMethod(final SelectDropDown o, final Select select, final CommandResult result) {
        if (o.getIndex() != null) {
            result.put("type", "select by index");
            select.selectByIndex(o.getIndex());
        } else if (o.getText() != null) {
            result.put("type", "select by visible text");
            select.selectByVisibleText(inject(o.getText()));
        } else {
            result.put("type", "select by value");
            select.selectByValue(inject(o.getValue()));
        }
    }
    //CHECKSTYLE:ON
}
