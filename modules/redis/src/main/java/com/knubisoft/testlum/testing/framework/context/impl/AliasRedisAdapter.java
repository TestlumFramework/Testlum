package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnRedisEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.redis.RedisOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Redis;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnRedisEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasRedisAdapter implements AliasAdapter {

    private final RedisOperation redisOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Redis redis : integrations.getRedisIntegration().getRedis()) {
            if (redis.isEnabled()) {
                aliasMap.put(StorageName.REDIS
                        + DelimiterConstant.UNDERSCORE + redis.getAlias(), redisOperation);
            }
        }
    }
}
