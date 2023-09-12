package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.PostgresIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnPostgresEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        PostgresIntegration postgresIntegration = ConfigProvider.getDefaultIntegrations().getPostgresIntegration();
        if (Objects.nonNull(postgresIntegration)) {
            return IntegrationsUtil.isEnabled(postgresIntegration.getPostgres());
        }
        return false;
    }
}
