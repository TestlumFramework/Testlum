package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Oracle;
import com.knubisoft.e2e.testing.model.global_config.Oracles;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnOracleEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Oracles oracles = GlobalTestConfigurationProvider.getIntegrations().getOracles();
        if (Objects.nonNull(oracles)) {
            return oracles.getOracle()
                    .stream().anyMatch(Oracle::isEnabled);
        }
        return false;
    }
}
