package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.framework.util.WaitUtil;
import com.knubisoft.e2e.testing.model.scenario.Wait;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.WAIT_INFO_LOG;
import static com.knubisoft.e2e.testing.framework.util.ResultUtil.TIME;

@Slf4j
@InterpreterForClass(Wait.class)
public class WaitInterpreter extends AbstractInterpreter<Wait> {

    public WaitInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    @SneakyThrows
    protected void acceptImpl(final Wait o, final CommandResult result) {
        log.info(WAIT_INFO_LOG, o.getTime(), o.getUnit());
        result.put(TIME, o.getTime());
        WaitUtil.getTimeUnit(o.getUnit(), result).sleep(o.getTime().longValue());
    }
}
