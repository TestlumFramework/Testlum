package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAliasImpl;
import com.knubisoft.testlum.testing.framework.db.sqs.SQSOperation;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.SQS;

@Conditional({OnSQSEnabledCondition.class})
@Component
public class AliasSQSAdapter implements AliasAdapter {

    @Autowired(required = false)
    private SQSOperation sqsOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sqs sqs : ConfigProvider.getDefaultIntegrations().getSqsIntegration().getSqs()) {
            if (sqs.isEnabled()) {
                aliasMap.put(SQS + UNDERSCORE + sqs.getAlias(), getMetadataSQS(sqs));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataSQS(final Sqs sqs) {
        return NameToAdapterAliasImpl.Metadata.builder()
                .configuration(sqs)
                .storageOperation(sqsOperation)
                .build();
    }
}
