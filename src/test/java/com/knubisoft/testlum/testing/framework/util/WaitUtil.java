package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.report.CommandResult;
import com.knubisoft.testlum.testing.model.scenario.Unit;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNKNOWN_TYPE;

@UtilityClass
public class WaitUtil {

    public TimeUnit getTimeUnit(final Unit unit, final CommandResult result) {
        switch (unit) {
            case MILLIS:
                result.put("Unit", "milliseconds");
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                result.put("Unit", "seconds");
                return TimeUnit.SECONDS;
            default:
                throw new DefaultFrameworkException(UNKNOWN_TYPE, unit.value());
        }
    }
}
