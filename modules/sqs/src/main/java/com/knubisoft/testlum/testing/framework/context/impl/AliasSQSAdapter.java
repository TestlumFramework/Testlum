package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSQSEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.sqs.SQSOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
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
