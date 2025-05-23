package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.redis.RedisOperation;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.REDIS;

@Conditional({OnRedisEnabledCondition.class})
@Component
public class AliasRedisAdapter implements AliasAdapter {

    @Autowired(required = false)
    private RedisOperation redisOperation;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Redis redis : GlobalTestConfigurationProvider.getDefaultIntegrations().getRedisIntegration().getRedis()) {
            if (redis.isEnabled()) {
                aliasMap.put(REDIS + UNDERSCORE + redis.getAlias(), redisOperation);
            }
        }
    }
}
