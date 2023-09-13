package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.mongodb.MongoOperation;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.MONGODB;

@Conditional({OnMongoEnabledCondition.class})
@Component
public class AliasMongoAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MongoOperation mongoOperation;

    @Override
    public void apply(final Map<String, StorageOperation> aliasMap) {
        for (Mongo mongo : GlobalTestConfigurationProvider.getDefaultIntegrations().getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                aliasMap.put(MONGODB + UNDERSCORE + mongo.getAlias(), mongoOperation);
            }
        }
    }
}
