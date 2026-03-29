package com.knubisoft.testlum.testing.framework.interpreter.lib;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InterpreterScannerTest {

    private final InterpreterScanner interpreterScanner = new InterpreterScanner();

    @Test
    void getInterpretersReturnsNonNullMap() {
        CommandToInterpreterClassMap result = interpreterScanner.getInterpreters();

        assertNotNull(result);
    }

    @Test
    void getInterpretersReturnsSameInstanceOnMultipleCalls() {
        CommandToInterpreterClassMap first = interpreterScanner.getInterpreters();
        CommandToInterpreterClassMap second = interpreterScanner.getInterpreters();

        assertSame(first, second);
    }

    @Test
    void getInterpretersReturnsNonEmptyMap() {
        CommandToInterpreterClassMap result = interpreterScanner.getInterpreters();

        assertFalse(result.isEmpty());
    }
}
