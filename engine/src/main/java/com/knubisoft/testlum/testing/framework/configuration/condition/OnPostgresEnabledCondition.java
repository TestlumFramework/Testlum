package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.PostgresIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnPostgresEnabledCondition implements Condition {

    private final PostgresIntegration postgresIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getPostgresIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(postgresIntegration)) {
            return IntegrationsUtil.isEnabled(postgresIntegration.getPostgres());
        }
        return false;
    }
}
