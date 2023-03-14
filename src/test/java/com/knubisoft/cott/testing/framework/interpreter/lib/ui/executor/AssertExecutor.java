package com.knubisoft.cott.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.EMPTY;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.NEW_LINE;
import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.SPACE;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.RETHROWN_ERRORS_LOG;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_ATTRIBUTE;
import static com.knubisoft.cott.testing.framework.util.ResultUtil.ASSERT_LOCATOR;

@ExecutorForClass(Assert.class)
public class AssertExecutor extends AbstractUiExecutor<Assert> {

    private int subCommandCounter;
    private List<String> exceptionResult = new ArrayList<>();

    public AssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final Assert aAssert, final CommandResult result) {
        List<CommandResult> subCommandResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandResult);
        aAssert.getAttribute().forEach(attribute -> executeAttributeCommand(attribute, subCommandResult));
        rethrowOnErrors();
    }

    private void executeAttributeCommand(final Attribute attribute,
                                         final List<CommandResult> subCommandsResult) {
        String actual = getActualValue(attribute);
        String expected = inject(attribute.getContent()).replaceAll(SPACE, EMPTY).replaceAll(NEW_LINE, EMPTY);
        LogUtil.logAttributeInfo(attribute, ++subCommandCounter);
        CommandResult subCommandResult = createSubCommandResult(attribute, actual, expected);
        executeComparison(subCommandsResult, actual, expected, subCommandResult);
    }

    private void executeComparison(final List<CommandResult> subCommandsResult,
                                   final String actual,
                                   final String expected,
                                   final CommandResult subCommandResult) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition())
                    .withActual(actual)
                    .withExpected(expected)
                    .exec();
        } catch (Exception e) {
            LogUtil.logException(e);
            exceptionResult.add(e.getMessage());
            subCommandResult.setException(e);
            subCommandResult.setSuccess(false);
        } finally {
            subCommandsResult.add(subCommandResult);
        }
    }

    private CommandResult createSubCommandResult(final Attribute attribute,
                                                 final String actual,
                                                 final String expected) {
        CommandResult subCommandResult = ResultUtil.createCommandResultForUiSubCommand(
                dependencies.getPosition().intValue(),
                attribute.getClass().getSimpleName(),
                "Execution of sub assert command");
        subCommandResult.put(ASSERT_LOCATOR, attribute.getLocatorId());
        subCommandResult.put(ASSERT_ATTRIBUTE, attribute.getName());
        subCommandResult.setActual(actual);
        subCommandResult.setExpected(expected);
        UiUtil.takeScreenshotAndSaveIfRequired(subCommandResult, dependencies);
        return subCommandResult;
    }

    private String getActualValue(final Attribute attribute) {
        WebElement webElement = UiUtil.findWebElement(dependencies, inject(attribute.getLocatorId()));
        UiUtil.waitForElementVisibility(dependencies, webElement);
        String value = UiUtil.getElementAttribute(webElement, inject(attribute.getName()));
        return value
                .replaceAll(SPACE, EMPTY)
                .replaceAll(NEW_LINE, EMPTY);
    }

    public void rethrowOnErrors() {
        if (!exceptionResult.isEmpty()) {
            throw new DefaultFrameworkException(RETHROWN_ERRORS_LOG,
                    String.join(DelimiterConstant.SPACE_WITH_LF, exceptionResult));
        }
    }


}
