package com.knubisoft.cott.testing.framework.util;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ScenarioUtil {

    private static final boolean STOP_SCENARIO_ON_FAILURE;

    static {
        STOP_SCENARIO_ON_FAILURE = GlobalTestConfigurationProvider.provide().isStopScenarioOnFailure();
    }

    public void checkIfStopScenarioOnFailure(final Exception e) {
        if (STOP_SCENARIO_ON_FAILURE) {
            throw new DefaultFrameworkException(e);
        }
    }
}
