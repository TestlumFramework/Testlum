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

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnSendgridEnabledCondition.class})
@Component
public class AliasSendGridAdapter implements AliasAdapter {

    @Autowired
    private SendGridOperation sendGridOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getSendgridIntegration().getSendgrid(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Sendgrid> sendgrids,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Sendgrid sendgrid : sendgrids) {
            if (sendgrid.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + sendgrid.getAlias(), getMetadataSendGrid(sendgrid));
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
