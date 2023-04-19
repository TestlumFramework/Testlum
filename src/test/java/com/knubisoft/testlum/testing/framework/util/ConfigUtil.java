package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigUtil {

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
