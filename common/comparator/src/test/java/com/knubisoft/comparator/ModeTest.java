package com.knubisoft.comparator;

import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link Mode} enum verifying onStrict callback execution in STRICT and LENIENT modes. */
class ModeTest {

    @Test
    void onStrictExecutesRunnableInStrictMode() {
        AtomicBoolean executed = new AtomicBoolean(false);
        Mode.STRICT.onStrict(() -> executed.set(true));
        assertTrue(executed.get());
    }

    @Test
    void onStrictDoesNotExecuteRunnableInLenientMode() {
        AtomicBoolean executed = new AtomicBoolean(false);
        Mode.LENIENT.onStrict(() -> executed.set(true));
        assertFalse(executed.get());
    }
}
