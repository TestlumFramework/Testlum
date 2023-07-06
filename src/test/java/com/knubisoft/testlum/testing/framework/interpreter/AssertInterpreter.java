package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Assert;
import com.knubisoft.testlum.testing.model.scenario.Equal;
import com.knubisoft.testlum.testing.model.scenario.Equality;
import com.knubisoft.testlum.testing.model.scenario.NotEqual;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.LinkedList;
import java.util.List;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_CONTENT_IS_EQUAL;
import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.ASSERT_CONTENT_NOT_EQUAL;


@Slf4j
@InterpreterForClass(Assert.class)
public class AssertInterpreter extends AbstractInterpreter<Assert> {

    public AssertInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Assert o, final CommandResult result) {
        Assert anAssert = injectCommand(o);
        List<CommandResult> subCommandsResult = new LinkedList<>();
        result.setSubCommandsResult(subCommandsResult);
        anAssert.getEqualOrNotEqual().forEach(action -> {
            int commandId = dependencies.getPosition().incrementAndGet();
            CommandResult commandResult = ResultUtil.newCommandResultInstance(commandId, action);
            subCommandsResult.add(commandResult);
            LogUtil.logSubCommand(commandId, action);
            processEachAction(action, commandResult);
        });
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final Equality action, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            processEqualityAction(action, result);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

    private void processEqualityAction(final Equality action, final CommandResult result) {
        if (action instanceof Equal) {
            checkContentIsEqual((Equal) action, result);
        } else {
            checkContentNotEqual((NotEqual) action, result);
        }
    }

    private void checkContentIsEqual(final Equal equal, final CommandResult result) {
        if (equal.getContent().stream().distinct().count() != 1) {
            throw new DefaultFrameworkException(ASSERT_CONTENT_NOT_EQUAL);
        }
        LogUtil.logAssertEqualCommand(equal);
        ResultUtil.addAssertEqualResult(equal, result);
    }

    private void checkContentNotEqual(final NotEqual notEqual, final CommandResult result) {
        List<String> content = notEqual.getContent();
        if (content.stream().distinct().count() != content.size()) {
            throw new DefaultFrameworkException(ASSERT_CONTENT_IS_EQUAL);
        }
        LogUtil.logAssertNotEqualCommand(notEqual);
        ResultUtil.addAssertNotEqualResult(notEqual, result);
    }

}
