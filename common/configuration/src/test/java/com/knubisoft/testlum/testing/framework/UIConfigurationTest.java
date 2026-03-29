package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.model.global_config.UiConfig;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class UIConfigurationTest {

    @Test
    void createsFromMap() {
        UiConfig dev = new UiConfig();
        UiConfig staging = new UiConfig();
        UIConfiguration config = new UIConfiguration(Map.of("dev", dev, "staging", staging));

        assertEquals(2, config.size());
        assertNotNull(config.get("dev"));
        assertNotNull(config.get("staging"));
    }

    @Test
    void createsFromEmptyMap() {
        UIConfiguration config = new UIConfiguration(Map.of());
        assertTrue(config.isEmpty());
    }

    @Test
    void supportsHashMapOperations() {
        UIConfiguration config = new UIConfiguration(Map.of("dev", new UiConfig()));
        config.put("prod", new UiConfig());
        assertEquals(2, config.size());
        assertTrue(config.containsKey("prod"));
    }
}
