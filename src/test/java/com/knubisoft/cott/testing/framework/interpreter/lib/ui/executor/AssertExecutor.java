package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.cott.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.cott.testing.framework.report.CommandResult;
import com.knubisoft.cott.testing.framework.util.LogUtil;
import com.knubisoft.cott.testing.framework.util.ResultUtil;
import com.knubisoft.cott.testing.framework.util.UiUtil;
import com.knubisoft.cott.testing.model.scenario.Assert;
import com.knubisoft.cott.testing.model.scenario.Attribute;
import org.openqa.selenium.WebElement;

import java.util.LinkedList;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_ATTRIBUTE;

@ExecutorForClass(Assert.class)
public class AssertExecutor extends AbstractUiExecutor<Assert> {


    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Assert aAssert, final CommandResult result) {
        List<CommandResult> subCommandResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandResult);
        aAssert.getAttribute().forEach(attribute -> executeAttributeCommand(attribute, subCommandResult));

    }

    private void executeAttributeCommand(final Attribute attribute,
                                         final List<CommandResult> subCommandsResult) {
        CommandResult subCommandResult =
                ResultUtil.createNewCommandResultInstance(dependencies.getPosition().intValue());
        LogUtil.logAttributeInfo(attribute);
        subCommandResult.put(ASSERT_ATTRIBUTE, attribute.getName());
        String actual = getActualValue(attribute);
        String expected = inject(attribute.getContent()).replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
        subCommandResult.setActual(actual);
        subCommandResult.setExpected(expected);
        new CompareBuilder(dependencies.getFile(), dependencies.getPosition())
                .withActual(actual)
                .withExpected(expected)
                .exec();
        UiUtil.takeScreenshotAndSaveIfRequired(subCommandResult, dependencies);
        subCommandsResult.add(subCommandResult);
    }

    private String getActualValue(final Assert aAssert) {
        WebElement webElement = UiUtil.findWebElement(dependencies, aAssert.getLocatorId());
        UiUtil.waitForElementVisibility(dependencies, webElement);
        String value = UiUtil.getElementAttribute(webElement, aAssert.getAttribute());
    private String getActualValue(final Attribute attribute) {
        WebElement webElement = UiUtil.findWebElement(dependencies, inject(attribute.getLocatorId()));
        String value = UiUtil.getElementAttribute(webElement, inject(attribute.getName()));
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }


}
