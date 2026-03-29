package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link CommandToInterpreterClassMap} verifying map creation,
 * entry storage, and retrieval.
 */
class CommandToInterpreterClassMapTest {

    @Test
    void createEmptyMap() {
        final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
        assertNotNull(map);
        assertTrue(map.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void putAndGet() {
        final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
        final Class<? extends AbstractCommand> key = AbstractCommand.class;
        final Class<AbstractInterpreter<? extends AbstractCommand>> value =
                (Class<AbstractInterpreter<? extends AbstractCommand>>) (Class<?>) AbstractInterpreter.class;

        map.put(key, value);

        assertEquals(value, map.get(key));
        assertEquals(1, map.size());
    }

    @Test
    void getMissingKeyReturnsNull() {
        final CommandToInterpreterClassMap map = new CommandToInterpreterClassMap();
        assertNull(map.get(AbstractCommand.class));
    }
}
