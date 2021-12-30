package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.db.mongodb.MongoOperation;
import com.knubisoft.e2e.testing.model.global_config.Mongo;
import com.knubisoft.e2e.testing.model.scenario.StorageName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnMongoEnabledCondition.class})
@Component
public class AliasMongoAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MongoOperation mongoOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Mongo mongo : GlobalTestConfigurationProvider.provide().getMongos().getMongo()) {
            if (mongo.isEnabled()) {
                aliasMap.put(StorageName.MONGODB + DelimiterConstant.UNDERSCORE + mongo.getAlias(), getMetadataMongo(mongo));
            }
        }

    }

    private NameToAdapterAlias.Metadata getMetadataMongo(final Mongo mongodb) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(mongodb)
                .storageOperation(mongoOperation)
                .build();
    }
}
