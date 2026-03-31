package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSendgridEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.sendgrid.SendGridOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnSendgridEnabledCondition.class})
@Component
public class AliasSendGridAdapter extends AbstractAliasAdapter {

    public AliasSendGridAdapter(final SendGridOperation sendGridOperation,
                                final Integrations integrations) {
        super(sendGridOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getSendgridIntegration().getSendgrid();
    }

    @Override
    protected String getStorageName() {
        return "Sendgrid";
    }
}
