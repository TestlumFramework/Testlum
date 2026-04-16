package com.knubisoft.testlum.testing.framework.interpreter.lib.ui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CommandToExecutorClassMap} verifying map creation and
 * empty state behavior.
 */
class CommandToExecutorClassMapTest {

    @Test
    void createEmptyMap() {
        final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    void mapSupportsSizeQuery() {
        final CommandToExecutorClassMap map = new CommandToExecutorClassMap();
        assertTrue(map.size() == 0);
    }
}
