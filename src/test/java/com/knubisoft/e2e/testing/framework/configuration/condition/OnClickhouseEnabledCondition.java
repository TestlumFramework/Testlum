package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Clickhouse;
import com.knubisoft.e2e.testing.model.global_config.Clickhouses;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnClickhouseEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Clickhouses clickhouses = GlobalTestConfigurationProvider.provide().getClickhouses();
        if (Objects.nonNull(clickhouses)) {
            return clickhouses
                    .getClickhouse().stream().anyMatch(Clickhouse::isEnabled);
        }
        return false;
    }
}
