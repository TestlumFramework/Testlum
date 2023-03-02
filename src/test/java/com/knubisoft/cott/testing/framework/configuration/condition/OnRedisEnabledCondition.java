package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.RedisIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRedisEnabledCondition implements Condition {

    private final RedisIntegration redisIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getRedisIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(redisIntegration)) {
            return ConfigUtil.isIntegrationEnabled(redisIntegration.getRedis());
        }
        return false;
    }
}
