package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExecutorScannerTest {

    private final ExecutorScanner executorScanner = new ExecutorScanner();

    @Test
    void getExecutorsReturnsNonNullMap() {
        CommandToExecutorClassMap result = executorScanner.getExecutors();

        assertNotNull(result);
    }

    @Test
    void getExecutorsReturnsSameInstanceOnMultipleCalls() {
        CommandToExecutorClassMap first = executorScanner.getExecutors();
        CommandToExecutorClassMap second = executorScanner.getExecutors();

        assertSame(first, second);
    }

    @Test
    void getExecutorsReturnsNonEmptyMap() {
        CommandToExecutorClassMap result = executorScanner.getExecutors();

        assertFalse(result.isEmpty());
    }
}
