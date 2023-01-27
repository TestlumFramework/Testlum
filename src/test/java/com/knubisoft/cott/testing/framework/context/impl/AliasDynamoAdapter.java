package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.dynamodb.DynamoDBOperation;
import com.knubisoft.cott.testing.framework.configuration.condition.OnDynamoEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.model.global_config.Dynamo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.DYNAMO;

@Component
@Conditional({OnDynamoEnabledCondition.class})
public class AliasDynamoAdapter implements AliasAdapter {

    @Autowired(required = false)
    private DynamoDBOperation dynamoDBOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getDynamoIntegration().getDynamo(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Dynamo> dynamoList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Dynamo dynamo : dynamoList) {
            if (dynamo.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + DYNAMO + UNDERSCORE + dynamo.getAlias(), getMetadataDynamo(dynamo));
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
