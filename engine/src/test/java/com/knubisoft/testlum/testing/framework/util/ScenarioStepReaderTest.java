package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.scenario.*;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ScenarioStepReaderTest {

    private Scenario createScenarioWithCommands(final AbstractCommand... commands) {
        Scenario scenario = mock(Scenario.class);
        when(scenario.getCommands()).thenReturn(Arrays.asList(commands));
        return scenario;
    }

    @Nested
    class WebCommand {

        @Test
        void shouldDetectWebCommand() {
            Web webCommand = mock(Web.class);
            ScenarioStepReader reader = new ScenarioStepReader(createScenarioWithCommands(webCommand));

            assertTrue(reader.isWeb());
            assertFalse(reader.isMobileBrowser());
            assertFalse(reader.isNatives());
        }
    }

    @Nested
    class MobileBrowserCommand {

        @Test
        void shouldDetectMobileBrowserCommand() {
            Mobilebrowser mobilebrowserCommand = mock(Mobilebrowser.class);
            ScenarioStepReader reader = new ScenarioStepReader(createScenarioWithCommands(mobilebrowserCommand));

            assertFalse(reader.isWeb());
            assertTrue(reader.isMobileBrowser());
            assertFalse(reader.isNatives());
        }
    }

    @Nested
    class NativeCommand {

        @Test
        void shouldDetectNativeCommand() {
            Native nativeCommand = mock(Native.class);
            ScenarioStepReader reader = new ScenarioStepReader(createScenarioWithCommands(nativeCommand));

            assertFalse(reader.isWeb());
            assertFalse(reader.isMobileBrowser());
            assertTrue(reader.isNatives());
        }
    }

    @Test
    void shouldDetectAllCommandTypes() {
        Web webCommand = mock(Web.class);
        Mobilebrowser mobilebrowserCommand = mock(Mobilebrowser.class);
        Native nativeCommand = mock(Native.class);
        ScenarioStepReader reader = new ScenarioStepReader(
                createScenarioWithCommands(webCommand, mobilebrowserCommand, nativeCommand));

        assertTrue(reader.isWeb());
        assertTrue(reader.isMobileBrowser());
        assertTrue(reader.isNatives());
    }

    @Test
    void shouldReturnAllFalseWhenNoCommands() {
        Scenario scenario = mock(Scenario.class);
        when(scenario.getCommands()).thenReturn(Collections.emptyList());
        ScenarioStepReader reader = new ScenarioStepReader(scenario);

        assertFalse(reader.isWeb());
        assertFalse(reader.isMobileBrowser());
        assertFalse(reader.isNatives());
    }

    @Test
    void shouldReturnAllFalseForOtherCommandTypes() {
        AbstractCommand otherCommand = mock(AbstractCommand.class);
        ScenarioStepReader reader = new ScenarioStepReader(createScenarioWithCommands(otherCommand));

        assertFalse(reader.isWeb());
        assertFalse(reader.isMobileBrowser());
        assertFalse(reader.isNatives());
    }
}
