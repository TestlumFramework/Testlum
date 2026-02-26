package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import com.knubisoft.testlum.testing.model.global_config.RedisIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnRedisEnabledCondition extends AbstractCondition<Redis> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getRedisIntegration())
                .map(RedisIntegration::getRedis)
                .orElse(null);
    }
}
