package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnMongoEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.mongodb.MongoOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnMongoEnabledCondition.class})
@Component
public class AliasMongoAdapter extends AbstractAliasAdapter {

    public AliasMongoAdapter(final MongoOperation mongoOperation,
                             final Integrations integrations) {
        super(mongoOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getMongoIntegration().getMongo();
    }

    @Override
    protected String getStorageName() {
        return StorageName.MONGODB.value();
    }
}
