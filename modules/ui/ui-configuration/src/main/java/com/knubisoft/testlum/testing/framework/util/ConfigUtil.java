package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class ConfigUtil {

    private final GlobalTestConfiguration globalTestConfiguration;

    public void checkIfStopScenarioOnFailure(final Exception e) {
        if (globalTestConfiguration.isStopScenarioOnFailure()) {
            throw new DefaultFrameworkException(e);
        }
    }
}
