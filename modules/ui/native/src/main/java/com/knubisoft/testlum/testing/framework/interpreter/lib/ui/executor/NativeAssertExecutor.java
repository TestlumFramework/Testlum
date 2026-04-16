package com.knubisoft.testlum.testing.framework.interpreter.lib.ui.executor;

import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.lib.CompareBuilder;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.AbstractUiExecutor;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.ui.ExecutorForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import com.knubisoft.testlum.testing.model.scenario.AssertAttribute;
import com.knubisoft.testlum.testing.model.scenario.AssertEqual;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import com.knubisoft.testlum.testing.model.scenario.AssertNotEqual;
import com.knubisoft.testlum.testing.model.scenario.NativeAssert;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebElement;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@ExecutorForClass(NativeAssert.class)
public class NativeAssertExecutor extends AbstractUiExecutor<NativeAssert> {

    public NativeAssertExecutor(final ExecutorDependencies dependencies) {
        super(dependencies);
    }

    @Override
    public void execute(final NativeAssert aAssert, final CommandResult result) {
        List<CommandResult> subCommandsResult = new ArrayList<>();
        result.setSubCommandsResult(subCommandsResult);
        aAssert.getAttributeOrEqualOrNotEqual().forEach(command -> {
            CommandResult commandResult =
                    resultUtil.newUiCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
            subCommandsResult.add(commandResult);
            logUtil.logAssertCommand(command, dependencies.getPosition().get());
            if (conditionUtil.isTrue(command.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                processEachCommand(command, commandResult);
            }
        });
    }

    private void processEachCommand(final AbstractCommand command, final CommandResult result) {
        if (command instanceof AssertAttribute aa) {
            executeAttributeCommand(aa, result);
        } else if (command instanceof AssertEquality ae) {
            executeEqualityCommand(ae, result);
        }
    }

    private void executeAttributeCommand(final AssertAttribute attribute, final CommandResult result) {
        logUtil.logAssertAttributeInfo(attribute);
        resultUtil.addAssertAttributeMetaData(attribute, result);
        String actual = getActualValue(attribute);
        String expected = attribute.getContent();
        resultUtil.setExpectedActual(expected, actual, result);
        executeComparison(actual, expected, result);
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private String getActualValue(final AssertAttribute attribute) {
        WebElement webElement = uiUtil.findWebElement(dependencies, attribute.getLocator(),
                attribute.getLocatorStrategy());
        return webElement.getAttribute(attribute.getName());
    }

    private void executeComparison(final String actual, final String expected, final CommandResult result) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get(),
                    jacksonService, stringPrettifier)
                    .withActual(actual)
                    .withExpected(expected)
                    .exec();
        } catch (Exception e) {
            logUtil.logException(e);
            resultUtil.setExceptionResult(result, e);
        }
    }

    private void executeEqualityCommand(final AssertEquality action, final CommandResult result) {
        log.info(LogMessage.CONTENT_LOG, String.join(DelimiterConstant.COMMA, action.getContent()));
        result.put(ResultUtil.CONTENT, String.join(DelimiterConstant.COMMA, action.getContent()));
        if (action instanceof AssertEqual equal) {
            checkContentIsEqual(equal);
        } else {
            checkContentNotEqual((AssertNotEqual) action);
        }
    }

    private void checkContentIsEqual(final AssertEqual equal) {
        AssertEqualityHelper.checkContentIsEqual(equal);
    }

    private void checkContentNotEqual(final AssertNotEqual notEqual) {
        AssertEqualityHelper.checkContentNotEqual(notEqual);
    }

}
