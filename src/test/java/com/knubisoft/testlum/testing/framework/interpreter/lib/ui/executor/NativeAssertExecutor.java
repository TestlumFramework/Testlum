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

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.COMMA;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONTENT_LOG;
import static com.knubisoft.testlum.testing.framework.interpreter.AssertInterpreter.ASSERT_CONTENT_IS_EQUAL;
import static com.knubisoft.testlum.testing.framework.interpreter.AssertInterpreter.ASSERT_CONTENT_NOT_EQUAL;
import static com.knubisoft.testlum.testing.framework.util.ResultUtil.CONTENT;

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
                    ResultUtil.newUiCommandResultInstance(dependencies.getPosition().incrementAndGet(), command);
            subCommandsResult.add(commandResult);
            LogUtil.logAssertCommand(command, dependencies.getPosition().get());
            if (ConditionUtil.isTrue(command.getCondition(), dependencies.getScenarioContext(), commandResult)) {
                processEachCommand(command, commandResult);
            }
        });
    }

    private void processEachCommand(final AbstractCommand command, final CommandResult result) {
        if (command instanceof AssertAttribute) {
            executeAttributeCommand((AssertAttribute) command, result);
        } else if (command instanceof AssertEquality) {
            executeEqualityCommand((AssertEquality) command, result);
        }
    }

    private void executeAttributeCommand(final AssertAttribute attribute, final CommandResult result) {
        LogUtil.logAssertAttributeInfo(attribute);
        ResultUtil.addAssertAttributeMetaData(attribute, result);
        String actual = getActualValue(attribute);
        String expected = attribute.getContent();
        ResultUtil.setExpectedActual(expected, actual, result);
        executeComparison(actual, expected, result);
        uiUtil.takeScreenshotAndSaveIfRequired(result, dependencies);
    }

    private String getActualValue(final AssertAttribute attribute) {
        WebElement webElement = uiUtil.findWebElement(dependencies, attribute.getLocatorId());
        return webElement.getAttribute(attribute.getName());
    }

    private void executeComparison(final String actual, final String expected, final CommandResult result) {
        try {
            new CompareBuilder(dependencies.getFile(), dependencies.getPosition().get())
                    .withActual(actual)
                    .withExpected(expected)
                    .exec();
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
        }
    }

    private void executeEqualityCommand(final AssertEquality action, final CommandResult result) {
        log.info(CONTENT_LOG, String.join(COMMA, action.getContent()));
        result.put(CONTENT, String.join(COMMA, action.getContent()));
        if (action instanceof AssertEqual) {
            checkContentIsEqual((AssertEqual) action);
        } else {
            checkContentNotEqual((AssertNotEqual) action);
        }
    }

    private void checkContentIsEqual(final AssertEqual equal) {
        if (equal.getContent().stream().distinct().count() != 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_NOT_EQUAL, formatContent(equal)));
        }
    }

    private void checkContentNotEqual(final AssertNotEqual notEqual) {
        List<String> content = notEqual.getContent();
        if (content.stream().distinct().count() == 1) {
            throw new DefaultFrameworkException(String.format(ASSERT_CONTENT_IS_EQUAL, formatContent(notEqual)));
        }
    }

    private String formatContent(final AssertEquality action) {
        return String.join(COMMA, action.getContent());
    }

}
