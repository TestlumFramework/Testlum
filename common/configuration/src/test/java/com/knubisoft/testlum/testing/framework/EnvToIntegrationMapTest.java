package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class EnvToIntegrationMapTest {

    @Test
    void createsFromMap() {
        Integrations dev = new Integrations();
        Integrations staging = new Integrations();
        EnvToIntegrationMap map = new EnvToIntegrationMap(Map.of("dev", dev, "staging", staging));

        assertEquals(2, map.size());
        assertNotNull(map.get("dev"));
        assertNotNull(map.get("staging"));
    }

    @Test
    void createsFromEmptyMap() {
        EnvToIntegrationMap map = new EnvToIntegrationMap(Map.of());
        assertTrue(map.isEmpty());
    }

    @Test
    void supportsHashMapOperations() {
        EnvToIntegrationMap map = new EnvToIntegrationMap(Map.of("dev", new Integrations()));
        map.put("prod", new Integrations());
        assertEquals(2, map.size());
        assertTrue(map.containsKey("prod"));
    }
}
