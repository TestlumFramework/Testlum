package com.knubisoft.testlum.testing.framework.scenario;

import com.knubisoft.testlum.testing.model.scenario.Scenario;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ScenarioArguments} verifying builder pattern,
 * default values, and field mutability.
 */
class ScenarioArgumentsTest {

    @Test
    void builderCreatesInstance() {
        final ScenarioArguments args = ScenarioArguments.builder()
                .path("/test/path")
                .file(new File("test.xml"))
                .browser("chrome")
                .containsUiSteps(true)
                .build();

        assertEquals("/test/path", args.getPath());
        assertEquals("test.xml", args.getFile().getName());
        assertEquals("chrome", args.getBrowser());
        assertTrue(args.isContainsUiSteps());
    }

    @Test
    void builderDefaultValues() {
        final ScenarioArguments args = ScenarioArguments.builder().build();
        assertNull(args.getPath());
        assertNull(args.getFile());
        assertNull(args.getScenario());
        assertNull(args.getException());
        assertNull(args.getBrowser());
        assertNull(args.getVariations());
        assertFalse(args.isContainsUiSteps());
    }

    @Test
    void builderWithAllFields() {
        final Scenario scenario = new Scenario();
        final Exception exception = new RuntimeException("test");
        final Map<String, String> variations = Map.of("key", "val");

        final ScenarioArguments args = ScenarioArguments.builder()
                .path("/path")
                .file(new File("f.xml"))
                .scenario(scenario)
                .exception(exception)
                .browser("firefox")
                .mobileBrowserDevice("pixel")
                .nativeDevice("emulator")
                .variations(variations)
                .containsUiSteps(false)
                .build();

        assertEquals(scenario, args.getScenario());
        assertEquals(exception, args.getException());
        assertEquals("firefox", args.getBrowser());
        assertEquals("pixel", args.getMobileBrowserDevice());
        assertEquals("emulator", args.getNativeDevice());
        assertEquals(variations, args.getVariations());
    }

    @Test
    void environmentIsSettable() {
        final ScenarioArguments args = ScenarioArguments.builder().build();
        assertNull(args.getEnvironment());
        args.setEnvironment("staging");
        assertEquals("staging", args.getEnvironment());
    }
}
