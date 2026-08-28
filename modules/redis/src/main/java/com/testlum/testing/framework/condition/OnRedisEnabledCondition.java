package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.Redis;
import com.testlum.testing.model.global_config.RedisIntegration;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnRedisEnabledCondition extends AbstractCondition<Redis> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getRedisIntegration).map(RedisIntegration::getRedis);
    }
}
