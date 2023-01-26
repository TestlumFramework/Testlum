package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Oracle;
import com.knubisoft.cott.testing.model.global_config.OracleIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnOracleEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final OracleIntegration oracleIntegration =
                GlobalTestConfigurationProvider.getDefaultIntegration().getOracleIntegration();
        if (Objects.nonNull(oracleIntegration)) {
            return oracleIntegration.getOracle()
                    .stream().anyMatch(Oracle::isEnabled);
        }
        return false;
    }
}
