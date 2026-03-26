package com.knubisoft.testlum.testing.framework.wait.util;

import com.knubisoft.testlum.testing.model.scenario.Timeunit;

import java.util.concurrent.TimeUnit;

public interface WaitUtil {

    TimeUnit getTimeUnit(Timeunit unit);

    void sleep(long timeout, TimeUnit timeUnit);

}
