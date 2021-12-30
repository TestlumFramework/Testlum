package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Postgres;
import com.knubisoft.e2e.testing.model.global_config.Postgreses;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnPostgresEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Postgreses postgreses = GlobalTestConfigurationProvider.provide().getPostgreses();
        if (Objects.nonNull(postgreses)) {
            return postgreses
                    .getPostgres().stream().anyMatch(Postgres::isEnabled);
        }
        return false;
    }
}
