package com.knubisoft.e2e.testing.framework.interpreter;

import com.knubisoft.e2e.testing.framework.interpreter.lib.AbstractInterpreter;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterDependencies;
import com.knubisoft.e2e.testing.framework.interpreter.lib.InterpreterForClass;
import com.knubisoft.e2e.testing.model.scenario.Wait;
import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.UNKNOWN_TYPE;
import static com.knubisoft.e2e.testing.framework.util.LogMessage.WAIT_INFO_LOG;

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
        result.put("time", o.getTime());
        getTimeUnit(o, result).sleep(o.getTime().longValue());
    }

    //CHECKSTYLE:OFF
    private TimeUnit getTimeUnit(final Wait o, final CommandResult result) {
        switch (o.getUnit()) {
            case MILLIS:
                result.put("unit", "milliseconds");
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                result.put("unit", "seconds");
                return TimeUnit.SECONDS;
            default:
                throw new DefaultFrameworkException(UNKNOWN_TYPE, o.getUnit().value());
        }
    }
    //CHECKSTYLE:ON
}
