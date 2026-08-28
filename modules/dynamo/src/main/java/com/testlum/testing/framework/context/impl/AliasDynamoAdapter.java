package com.testlum.testing.framework.context.impl;

import com.testlum.testing.framework.condition.OnDynamoEnabledCondition;
import com.testlum.testing.framework.context.AbstractAliasAdapter;
import com.testlum.testing.framework.db.dynamodb.DynamoDBOperation;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnDynamoEnabledCondition.class})
@Component
public class AliasDynamoAdapter extends AbstractAliasAdapter {

    public AliasDynamoAdapter(final DynamoDBOperation dynamoDBOperation,
                              final Integrations integrations) {
        super(dynamoDBOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getDynamoIntegration().getDynamo();
    }

    @Override
    protected String getStorageName() {
        return StorageName.DYNAMO.value();
    }
}
