package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.RedisIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRedisEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        RedisIntegration redisIntegration = ConfigProvider.getDefaultIntegrations().getRedisIntegration();
        if (Objects.nonNull(redisIntegration)) {
            return IntegrationsUtil.isEnabled(redisIntegration.getRedis());
        }
        return false;
    }
}
