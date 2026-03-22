package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnDynamoEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.dynamodb.DynamoDBOperation;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnDynamoEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasDynamoAdapter implements AliasAdapter {

    private final DynamoDBOperation dynamoDBOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Dynamo dynamo : integrations.getDynamoIntegration().getDynamo()) {
            if (dynamo.isEnabled()) {
                aliasMap.put(StorageName.DYNAMO + DelimiterConstant.UNDERSCORE + dynamo.getAlias(), dynamoDBOperation);
            }
        }
    }
}
