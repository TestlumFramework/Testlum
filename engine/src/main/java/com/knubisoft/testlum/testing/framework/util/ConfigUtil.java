package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ConfigUtil {

    public void checkIfStopScenarioOnFailure(final Exception e) {
        if (GlobalTestConfigurationProvider.get().provide().isStopScenarioOnFailure()) {
            throw new DefaultFrameworkException(e);
        }
    }
}
