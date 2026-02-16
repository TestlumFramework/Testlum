package com.knubisoft.testlum.testing.framework.interpreter;

import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.testlum.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Wait;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

@Slf4j
@InterpreterForClass(Wait.class)
public class WaitInterpreter extends AbstractInterpreter<Wait> {

    private static final String TIME = "Time";
    private static final String TIME_UNITE = "Time unit";

    private static final String WAIT_INFO_LOG = LogFormat.table("Wait time & unit", "{} {}");

    @Autowired
    private WaitUtil waitUtil;

    public WaitInterpreter(final InterpreterDependencies dependencies) {
        super(dependencies);
    }

    @Override
    protected void acceptImpl(final Wait o, final CommandResult result) {
        Wait wait = injectCommand(o);
        String time = wait.getTime();
        log.info(WAIT_INFO_LOG, time, wait.getUnit());
        TimeUnit timeUnit = waitUtil.getTimeUnit(wait.getUnit());
        addWaitMetaData(time, timeUnit, result);
        waitUtil.sleep(Long.parseLong(time), timeUnit);
    }

    private void addWaitMetaData(final String time,
                                final TimeUnit unit,
                                final CommandResult result) {
        result.put(TIME, time);
        result.put(TIME_UNITE, unit.name());
    }
}
