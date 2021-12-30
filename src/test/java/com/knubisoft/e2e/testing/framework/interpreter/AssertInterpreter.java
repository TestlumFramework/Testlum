package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractSeleniumInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Assert;
import org.openqa.selenium.WebElement;

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.SPACE;

@InterpreterForClass(Assert.class)
public class AssertInterpreter extends AbstractSeleniumInterpreter<Assert> {

    public AssertInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Assert o, final CommandResult result) {
        String actual = getActual(o, result);
        String expected = getExpected(o);
        result.setExpected(expected);
        result.setActual(actual);
        newCompare()
                .withActual(actual)
                .withExpected(expected)
                .exec();
    }

    private String getActual(final Assert o, final CommandResult result) {
        result.setLocatorId(o.getLocatorId());
        WebElement element = getWebElement(o.getLocatorId());
        String value = element.getAttribute(o.getAttribute().value());
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }

    private String getExpected(final Assert o) {
        return o.getContent()
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }
}
