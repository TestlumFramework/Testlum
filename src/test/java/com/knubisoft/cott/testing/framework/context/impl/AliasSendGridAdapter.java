package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.sendgrid.SendGridOperation;
import com.knubisoft.cott.testing.model.global_config.Sendgrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.SENDGRID;

@Conditional({OnSendgridEnabledCondition.class})
@Component
public class AliasSendGridAdapter implements AliasAdapter {

    @Autowired
    private SendGridOperation sendGridOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sendgrid sendgrid
                : GlobalTestConfigurationProvider.getDefaultIntegration().getSendgridIntegration().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                aliasMap.put(SENDGRID + UNDERSCORE + sendgrid.getAlias(), getMetadataSendGrid(sendgrid));
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
