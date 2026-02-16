package com.knubisoft.testlum.testing.framework.wait.util;

import com.knubisoft.testlum.testing.model.scenario.Timeunit;

import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

public interface WaitUtil {

    TimeUnit getTimeUnit(Timeunit unit);

    void sleep(long timeout, TimeUnit timeUnit);

    void waitUntil(BooleanSupplier condition, long timeoutMillis, TimeUnit timeUnit, long period);
}
