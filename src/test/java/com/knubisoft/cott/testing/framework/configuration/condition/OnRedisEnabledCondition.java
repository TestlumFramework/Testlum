package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Redis;
import com.knubisoft.cott.testing.model.global_config.RedisIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRedisEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final RedisIntegration redisIntegration =
                GlobalTestConfigurationProvider.getDefaultIntegration().getRedisIntegration();
        if (Objects.nonNull(redisIntegration)) {
            return redisIntegration.getRedis()
                    .stream().anyMatch(Redis::isEnabled);
        }
        return false;
    }
}
