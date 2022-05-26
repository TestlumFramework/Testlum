package com.knubisoft.e2e.testing.framework.util;

import com.knubisoft.e2e.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.e2e.testing.framework.report.CommandResult;
import com.knubisoft.e2e.testing.model.scenario.Unit;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

import static com.knubisoft.e2e.testing.framework.util.LogMessage.UNKNOWN_TYPE;

@UtilityClass
public class WaitUtil {

    public TimeUnit getTimeUnit(final Unit unit, final CommandResult result) {
        switch (unit) {
            case MILLIS:
                result.put("unit", "milliseconds");
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                result.put("unit", "seconds");
                return TimeUnit.SECONDS;
            default:
                throw new DefaultFrameworkException(UNKNOWN_TYPE, unit.value());
        }
    }
}
