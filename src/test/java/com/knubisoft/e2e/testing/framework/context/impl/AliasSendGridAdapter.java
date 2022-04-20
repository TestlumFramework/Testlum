package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.sendgrid.SendGridOperation;
import com.knubisoft.e2e.testing.model.global_config.Sendgrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnSendgridEnabledCondition.class})
@Component
public class AliasSendGridAdapter implements AliasAdapter {

    @Autowired
    private SendGridOperation sendGridOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sendgrid sendgrid : GlobalTestConfigurationProvider.getIntegrations().getSendgrids().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                aliasMap.put(sendgrid.getAlias(), getMetadataSendGrid(sendgrid));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataSendGrid(final Sendgrid sendgrid) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(sendgrid)
                .storageOperation(sendGridOperation)
                .build();
    }
}
