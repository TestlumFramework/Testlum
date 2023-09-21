package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.TIME_UNIT_UNKNOWN_TYPE;

@Service
public class WaitUtilImpl implements WaitUtil {

    @Override
    public TimeUnit getTimeUnit(final Timeunit unit) {
        switch (unit) {
            case MILLIS:
                return TimeUnit.MILLISECONDS;
            case SECONDS:
                return TimeUnit.SECONDS;
            default:
                throw new DefaultFrameworkException(TIME_UNIT_UNKNOWN_TYPE, unit.value());
        }
    }

    @Override
    public void sleep(final long timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException ignored) {
            //ignored
        }
    }

    @Override
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
