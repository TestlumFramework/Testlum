package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.dynamodb.DynamoDBOperation;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.model.global_config.Dynamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.e2e.testing.model.scenario.StorageName.DYNAMO;

@Component
@Conditional({OnDynamoEnabledCondition.class})
public class AliasDynamoAdapter implements AliasAdapter {

    @Autowired(required = false)
    private DynamoDBOperation dynamoDBOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Dynamo dynamo : GlobalTestConfigurationProvider.getIntegrations().getDynamos().getDynamo()) {
            if (dynamo.isEnabled()) {
                aliasMap.put(DYNAMO + DelimiterConstant.UNDERSCORE + dynamo.getAlias(), getMetadataDynamo(dynamo));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataDynamo(final Dynamo dynamodb) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(dynamodb)
                .storageOperation(dynamoDBOperation)
                .build();
    }
}
