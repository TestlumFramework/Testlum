package com.testlum.testing.framework.context.impl;

import com.testlum.testing.framework.condition.OnSQSEnabledCondition;
import com.testlum.testing.framework.context.AbstractAliasAdapter;
import com.testlum.testing.framework.db.sqs.SQSOperation;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnSQSEnabledCondition.class})
@Component
public class AliasSQSAdapter extends AbstractAliasAdapter {

    public AliasSQSAdapter(final SQSOperation sqsOperation,
                           final Integrations integrations) {
        super(sqsOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getSqsIntegration().getSqs();
    }

    @Override
    protected String getStorageName() {
        return "SQS";
    }
}
