package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAliasImpl;
import com.knubisoft.testlum.testing.framework.db.dynamodb.DynamoDBOperation;
import com.knubisoft.testlum.testing.model.global_config.Dynamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.DYNAMO;

@Conditional({OnDynamoEnabledCondition.class})
@Component
public class AliasDynamoAdapter implements AliasAdapter {

    @Autowired(required = false)
    private DynamoDBOperation dynamoDBOperation;
    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Dynamo dynamo
                : globalTestConfigurationProvider.getDefaultIntegrations().getDynamoIntegration().getDynamo()) {
            if (dynamo.isEnabled()) {
                aliasMap.put(DYNAMO + UNDERSCORE + dynamo.getAlias(), getMetadataDynamo(dynamo));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataDynamo(final Dynamo dynamodb) {
        return NameToAdapterAliasImpl.Metadata.builder()
                .configuration(dynamodb)
                .storageOperation(dynamoDBOperation)
                .build();
    }
}
