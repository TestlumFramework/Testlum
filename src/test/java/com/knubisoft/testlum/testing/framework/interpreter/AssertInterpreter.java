package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.AssertHelper;
import com.knubisoft.testlum.testing.framework.util.ConfigUtil;
import com.knubisoft.testlum.testing.framework.util.LogUtil;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.model.scenario.Assert;
import com.knubisoft.testlum.testing.model.scenario.AssertEquality;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import java.util.LinkedList;
import java.util.List;


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
            LogUtil.logAssertCommand(action, commandId);
            ResultUtil.addAssertEqualityMetaData(action, commandResult);
            processEachAction(action, commandResult);
        });
        ResultUtil.setExecutionResultIfSubCommandsFailed(result);
    }

    private void processEachAction(final AssertEquality action, final CommandResult result) {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            AssertHelper.executeEqualityCommand(action, result);
        } catch (Exception e) {
            LogUtil.logException(e);
            ResultUtil.setExceptionResult(result, e);
            ConfigUtil.checkIfStopScenarioOnFailure(e);
        } finally {
            result.setExecutionTime(stopWatch.getTime());
            stopWatch.stop();
        }
    }

}
