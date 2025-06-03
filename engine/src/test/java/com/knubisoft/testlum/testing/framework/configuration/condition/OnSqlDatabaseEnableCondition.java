package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabaseIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSqlDatabaseEnableCondition implements Condition {
    private final SqlDatabaseIntegration sqlDatabaseIntegration = GlobalTestConfigurationProvider
            .getDefaultIntegrations().getSqlDatabaseIntegration();

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(sqlDatabaseIntegration)) {
            return IntegrationsProviderImpl.IntegrationsUtil.isEnabled(sqlDatabaseIntegration.getSqlDatabase());
        }
        return false;
    }
}