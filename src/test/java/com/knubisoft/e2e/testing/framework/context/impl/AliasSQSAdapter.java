package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.db.sqs.SQSOperation;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.model.global_config.Sqs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnSQSEnabledCondition.class})
@Component
public class AliasSQSAdapter implements AliasAdapter {

    @Autowired(required = false)
    private SQSOperation sqsOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sqs sqs : GlobalTestConfigurationProvider.getIntegrations().getSqss().getSqs()) {
            if (sqs.isEnabled()) {
                aliasMap.put(sqs.getAlias(), getMetadataSQS(sqs));
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
