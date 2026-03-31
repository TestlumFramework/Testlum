package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.wait.util.WaitUtil;
import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class WaitUtilImpl implements WaitUtil {

    @Override
    public TimeUnit getTimeUnit(final Timeunit unit) {
        return switch (unit) {
            case MILLIS -> TimeUnit.MILLISECONDS;
            case SECONDS -> TimeUnit.SECONDS;
        };
    }

    @Override
    public void sleep(final long timeout, final TimeUnit timeUnit) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException e) {
            log.warn("Sleep interrupted after {} {}", timeout, timeUnit, e);
            Thread.currentThread().interrupt();
        }
    }
}
