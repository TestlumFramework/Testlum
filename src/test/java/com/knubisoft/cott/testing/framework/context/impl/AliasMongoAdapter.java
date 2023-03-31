package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.mongodb.MongoOperation;
import com.knubisoft.cott.testing.model.global_config.Mongo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.MONGODB;

@Conditional({OnMongoEnabledCondition.class})
@Component
public class AliasMongoAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MongoOperation mongoOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Mongo mongo : GlobalTestConfigurationProvider.getDefaultIntegrations().getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                aliasMap.put(MONGODB + UNDERSCORE + mongo.getAlias(), getMetadataMongo(mongo));
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
