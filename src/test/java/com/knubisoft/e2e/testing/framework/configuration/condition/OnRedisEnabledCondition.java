package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Redis;
import com.knubisoft.e2e.testing.model.global_config.Redises;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRedisEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Redises redises = GlobalTestConfigurationProvider.getIntegrations().getRedises();
        if (Objects.nonNull(redises)) {
            return redises.getRedis()
                    .stream().anyMatch(Redis::isEnabled);
        }
        return false;
    }
}
