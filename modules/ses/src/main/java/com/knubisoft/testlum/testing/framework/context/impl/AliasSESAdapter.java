package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSESEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.ses.SESOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnSESEnabledCondition.class})
@Component
public class AliasSESAdapter extends AbstractAliasAdapter {

    public AliasSESAdapter(final SESOperation sesOperation,
                           final Integrations integrations) {
        super(sesOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getSesIntegration().getSes();
    }

    @Override
    protected String getStorageName() {
        return "SES";
    }
}
