package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnRedisEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.redis.RedisOperation;
import com.knubisoft.cott.testing.model.global_config.Redis;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.REDIS;

@Conditional({OnRedisEnabledCondition.class})
@Component
public class AliasRedisAdapter implements AliasAdapter {

    @Autowired(required = false)
    private RedisOperation redisOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
       GlobalTestConfigurationProvider.getIntegrations()
               .forEach(((s, integrations) -> addToAliasMap(s, integrations.getRedisIntegration().getRedis(),
                       aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Redis> redisList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Redis redis : redisList) {
            if (redis.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + REDIS + UNDERSCORE + redis.getAlias(), getMetadataRedis(redis));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataRedis(final Redis redis) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(redis)
                .storageOperation(redisOperation)
                .build();
    }

}
