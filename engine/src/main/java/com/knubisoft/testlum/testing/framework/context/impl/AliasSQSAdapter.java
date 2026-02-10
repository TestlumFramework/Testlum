package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
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
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Sqs sqs : GlobalTestConfigurationProvider.get().getDefaultIntegrations().getSqsIntegration().getSqs()) {
            if (sqs.isEnabled()) {
                aliasMap.put(SQS + UNDERSCORE + sqs.getAlias(), sqsOperation);
            }
        }
    }
}
