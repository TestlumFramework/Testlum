package com.knubisoft.testlum.testing.framework.interpreter.lib;

import com.knubisoft.testlum.testing.model.scenario.AbstractCommand;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class CommandToInterpreterMapTest {

    private CommandToInterpreterMap map;

    @BeforeEach
    void setUp() {
        map = new CommandToInterpreterMap();
    }

    @Test
    void shouldExtendLinkedHashMap() {
        assertInstanceOf(LinkedHashMap.class, map);
    }

    @Test
    void shouldBeEmptyInitially() {
        assertTrue(map.isEmpty());
        assertEquals(0, map.size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void shouldSupportPutAndGet() {
        Class<? extends AbstractCommand> commandClass = AbstractCommand.class;
        AbstractInterpreter<? extends AbstractCommand> interpreter = mock(AbstractInterpreter.class);

        map.put(commandClass, interpreter);

        assertEquals(1, map.size());
        assertSame(interpreter, map.get(commandClass));
    }

    @Test
    void shouldSupportContainsKey() {
        Class<? extends AbstractCommand> commandClass = AbstractCommand.class;
        AbstractInterpreter<? extends AbstractCommand> interpreter = mock(AbstractInterpreter.class);

        map.put(commandClass, interpreter);

        assertTrue(map.containsKey(commandClass));
    }

    @Test
    void shouldMaintainInsertionOrder() {
        AbstractInterpreter<? extends AbstractCommand> interpreter1 = mock(AbstractInterpreter.class);
        AbstractInterpreter<? extends AbstractCommand> interpreter2 = mock(AbstractInterpreter.class);

        map.put(AbstractCommand.class, interpreter1);

        assertEquals(1, map.size());
        assertFalse(map.isEmpty());
    }
}
