package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.OracleIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnOracleEnabledCondition implements Condition {

    private final OracleIntegration oracleIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getOracleIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(oracleIntegration)) {
            return IntegrationsUtil.isEnabled(oracleIntegration.getOracle());
        }
        return false;
    }
}
