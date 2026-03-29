package com.knubisoft.testlum.testing.framework.report;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScenarioResult} verifying field access, default values,
 * and command result collection.
 */
class ScenarioResultTest {

    @Test
    void defaultValues() {
        final ScenarioResult result = new ScenarioResult();
        assertEquals(0, result.getId());
        assertNull(result.getName());
        assertNull(result.getPath());
        assertNull(result.getBrowser());
        assertFalse(result.isSuccess());
        assertEquals(0, result.getExecutionTime());
        assertNotNull(result.getCommands());
        assertTrue(result.getCommands().isEmpty());
    }

    @Test
    void setAndGetAllFields() {
        final ScenarioResult result = new ScenarioResult();
        result.setId(1);
        result.setName("login-test");
        result.setPath("/scenarios/login.xml");
        result.setBrowser("chrome");
        result.setMobilebrowserDevice("pixel");
        result.setNativeDevice("emulator");
        result.setSuccess(true);
        result.setCause("passed");
        result.setExecutionTime(1500);
        result.setEnvironment("staging");
        result.setTags("smoke,regression");

        assertEquals(1, result.getId());
        assertEquals("login-test", result.getName());
        assertEquals("/scenarios/login.xml", result.getPath());
        assertEquals("chrome", result.getBrowser());
        assertEquals("pixel", result.getMobilebrowserDevice());
        assertEquals("emulator", result.getNativeDevice());
        assertTrue(result.isSuccess());
        assertEquals("passed", result.getCause());
        assertEquals(1500, result.getExecutionTime());
        assertEquals("staging", result.getEnvironment());
        assertEquals("smoke,regression", result.getTags());
    }

    @Test
    void commandsListIsMutable() {
        final ScenarioResult result = new ScenarioResult();
        final CommandResult cmd = new CommandResult();
        cmd.setId(1);
        cmd.setSuccess(true);
        result.getCommands().add(cmd);
        assertEquals(1, result.getCommands().size());
    }

    @Test
    void commandsCanBeReplaced() {
        final ScenarioResult result = new ScenarioResult();
        final CommandResult cmd1 = new CommandResult();
        final CommandResult cmd2 = new CommandResult();
        result.setCommands(List.of(cmd1, cmd2));
        assertEquals(2, result.getCommands().size());
    }
}
