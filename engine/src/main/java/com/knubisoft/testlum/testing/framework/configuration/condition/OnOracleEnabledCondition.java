package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import com.knubisoft.testlum.testing.model.global_config.OracleIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnOracleEnabledCondition extends AbstractCondition<Oracle> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getOracleIntegration())
                .map(OracleIntegration::getOracle)
                .orElse(null);
    }
}
