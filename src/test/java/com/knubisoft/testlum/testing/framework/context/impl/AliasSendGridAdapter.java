package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSendgridEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAliasImpl;
import com.knubisoft.testlum.testing.framework.db.sendgrid.SendGridOperation;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.SENDGRID;

@Conditional({OnSendgridEnabledCondition.class})
@Component
public class AliasSendGridAdapter implements AliasAdapter {

    @Autowired
    private SendGridOperation sendGridOperation;
    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sendgrid sendgrid
                : globalTestConfigurationProvider.getDefaultIntegrations().getSendgridIntegration().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                aliasMap.put(SENDGRID + UNDERSCORE + sendgrid.getAlias(), getMetadataSendGrid(sendgrid));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataSendGrid(final Sendgrid sendgrid) {
        return NameToAdapterAliasImpl.Metadata.builder()
                .configuration(sendgrid)
                .storageOperation(sendGridOperation)
                .build();
    }
}
