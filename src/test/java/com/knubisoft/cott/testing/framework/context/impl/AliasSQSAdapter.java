package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.db.sqs.SQSOperation;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.model.global_config.Dynamo;
import com.knubisoft.cott.testing.model.global_config.Sqs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnSQSEnabledCondition.class})
@Component
public class AliasSQSAdapter implements AliasAdapter {

    @Autowired(required = false)
    private SQSOperation sqsOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getSqsIntegration().getSqs(), aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Sqs> sqsList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sqs sqs : sqsList) {
            if (sqs.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + sqs.getAlias(), getMetadataSQS(sqs));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataSQS(final Sqs sqs) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(sqs)
                .storageOperation(sqsOperation)
                .build();
    }
}
