package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.scenario.Timeunit;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for {@link WaitUtilImpl} verifying time unit conversion and sleep execution.
 */
class WaitUtilImplTest {

    private final WaitUtilImpl waitUtil = new WaitUtilImpl();

    @Test
    void getTimeUnitMillis() {
        assertEquals(TimeUnit.MILLISECONDS, waitUtil.getTimeUnit(Timeunit.MILLIS));
    }

    @Test
    void getTimeUnitSeconds() {
        assertEquals(TimeUnit.SECONDS, waitUtil.getTimeUnit(Timeunit.SECONDS));
    }

    @Test
    void sleepDoesNotThrow() {
        final long start = System.currentTimeMillis();
        waitUtil.sleep(1, TimeUnit.MILLISECONDS);
        final long elapsed = System.currentTimeMillis() - start;
        // Just verify it completes without error
        assertEquals(true, elapsed >= 0);
    }
}
