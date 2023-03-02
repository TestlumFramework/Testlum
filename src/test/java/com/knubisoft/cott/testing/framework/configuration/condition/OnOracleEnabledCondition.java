package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.OracleIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnOracleEnabledCondition implements Condition {

    private final OracleIntegration oracleIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getOracleIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(oracleIntegration)) {
            return ConfigUtil.isIntegrationEnabled(oracleIntegration.getOracle());
        }
        return false;
    }
}
