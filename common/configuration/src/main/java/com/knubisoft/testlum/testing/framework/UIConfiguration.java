package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.model.global_config.UiConfig;

import java.util.HashMap;
import java.util.Map;

public class UIConfiguration extends HashMap<String, UiConfig> {
    public UIConfiguration(final Map<? extends String, ? extends UiConfig> m) {
        super(m);
    }
}
