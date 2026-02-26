package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.mongodb.MongoOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mongo;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.MONGODB;

@Conditional({OnMongoEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasMongoAdapter implements AliasAdapter {

    private final MongoOperation mongoOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Mongo mongo : integrations.getMongoIntegration().getMongo()) {
            if (mongo.isEnabled()) {
                aliasMap.put(MONGODB + UNDERSCORE + mongo.getAlias(), mongoOperation);
            }
        }
    }
}
