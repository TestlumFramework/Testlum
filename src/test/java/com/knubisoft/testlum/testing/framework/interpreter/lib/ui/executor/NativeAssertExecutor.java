package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConditionUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.UiUtil;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.NativeAssert;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ExecutorForClass(NativeAssert.class)
public class NativeAssertExecutor extends AbstractUiExecutor<NativeAssert> {

    private final List<String> exceptionResult = new ArrayList<>();
    private final AtomicInteger commandId = new AtomicInteger();

    public NativeAssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final NativeAssert aAssert, final CommandResult result) {
        List<CommandResult> subCommandsResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandsResult);
        aAssert.getAttribute().forEach(attribute -> {
            CommandResult commandResult = ResultUtil.newUiCommandResultInstance(commandId.incrementAndGet(), attribute);
            subCommandsResult.add(commandResult);
            LogUtil.logAssertCommand(attribute, commandId.get());
            if (ConditionUtil.isTrue(attribute.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                executeAttributeCommand(attribute, commandResult);
            }
        });
        rethrowOnErrors();
    }

    private void executeAttributeCommand(final AssertAttribute attribute, final CommandResult result) {
        LogUtil.logAssertAttributeInfo(attribute);
        ResultUtil.addAssertAttributeMetaData(attribute, result);
        String actual = getActualValue(attribute);
        String expected = attribute.getContent();
        ResultUtil.setExpectedActual(expected, actual, result);
        executeComparison(actual, expected, result);
        UiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private String getActualValue(final AssertAttribute attribute) {
        WebElement webElement = UiUtil.findWebElement(dependencies, attribute.getLocatorId());
        return webElement.getAttribute(attribute.getName());
    }

    private void executeComparison(final String actual, final String expected, final CommandResult result) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get())
                    .withActual(actual)
                    .withExpected(expected)
                    .exec();
        } catch (Exception e) {
            handleException(e, result);
        }
    }

    private void handleException(final Exception e, final CommandResult result) {
        LogUtil.logException(e);
        ResultUtil.setExceptionResult(result, e);
        exceptionResult.add(e.getMessage());
    }

    private void rethrowOnErrors() {
        if (!exceptionResult.isEmpty()) {
            throw new DefaultFrameworkException(exceptionResult);
        }
    }
}
