package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Postgres;
import com.knubisoft.cott.testing.model.global_config.PostgresIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnPostgresEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final PostgresIntegration postgresIntegration =
                GlobalTestConfigurationProvider.getDefaultIntegration().getPostgresIntegration();
        if (Objects.nonNull(postgresIntegration)) {
            return postgresIntegration.getPostgres().stream().anyMatch(Postgres::isEnabled);
        }
        return false;
    }
}
