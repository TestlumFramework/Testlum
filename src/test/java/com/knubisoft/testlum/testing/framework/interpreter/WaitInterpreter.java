package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.util.ResultUtil;
import com.knubisoft.testlum.testing.framework.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Wait;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WAIT_INFO_LOG;

@Slf4j
@InterpreterForClass(Wait.class)
public class WaitInterpreter extends AbstractInterpreter<Wait> {

    public WaitInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Wait o, final CommandResult result) {
        Wait wait = injectCommand(o);
        String time = wait.getTime();
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        TimeUnit timeUnit = WaitUtil.getTimeUnit(wait.getUnit());
        ResultUtil.addWaitMetaData(time, timeUnit, result);
        WaitUtil.sleep(Long.parseLong(time), timeUnit);
    }
}
