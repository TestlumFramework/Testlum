package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnRedisEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.redis.RedisOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnRedisEnabledCondition.class})
@Component
public class AliasRedisAdapter extends AbstractAliasAdapter {

    public AliasRedisAdapter(final RedisOperation redisOperation,
                             final Integrations integrations) {
        super(redisOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getRedisIntegration().getRedis();
    }

    @Override
    protected String getStorageName() {
        return StorageName.REDIS.value();
    }
}
