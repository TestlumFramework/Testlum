package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnSESEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.ses.SESOperation;
import com.knubisoft.cott.testing.model.global_config.Ses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.SES;

@Conditional({OnSESEnabledCondition.class})
@Component
public class AliasSESAdapter implements AliasAdapter {

    @Autowired(required = false)
    private SESOperation sesOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Ses ses : GlobalTestConfigurationProvider.getDefaultIntegrations().getSesIntegration().getSes()) {
            if (ses.isEnabled()) {
                aliasMap.put(SES + UNDERSCORE + ses.getAlias(), getMetadataSES(ses));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataSES(final Ses ses) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(ses)
                .storageOperation(sesOperation)
                .build();
    }
}
