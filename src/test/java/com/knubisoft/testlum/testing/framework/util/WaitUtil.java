package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.Unit;
import lombok.experimental.UtilityClass;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNKNOWN_TYPE;

@UtilityClass
public class WaitUtil {

    public TimeUnit getTimeUnit(final Unit unit) {
        switch (unit) {
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            default:
                throw new DefaultFrameworkException(UNKNOWN_TYPE, unit.value());
        }
    }

    public void sleep(final long timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException ignored) {
            //ignored
        }
    }

    public void waitUntil(final BooleanSupplier condition,
                          final long timeoutMillis,
                          final TimeUnit timeUnit,
                          final long period) {
        long start = System.currentTimeMillis();
        while (!condition.getAsBoolean()) {
            if (System.currentTimeMillis() - start > timeoutMillis) {
                return;
            }
            try {
                timeUnit.sleep(period);
            } catch (InterruptedException ignored) {
                //ignored
            }
        }
    }
}
