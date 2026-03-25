package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.model.global_config.Integrations;

import java.util.HashMap;
import java.util.Map;

public class EnvToIntegrationMap extends HashMap<String, Integrations> {
    public EnvToIntegrationMap(final Map<? extends String, ? extends Integrations> m) {
        super(m);
    }
}
