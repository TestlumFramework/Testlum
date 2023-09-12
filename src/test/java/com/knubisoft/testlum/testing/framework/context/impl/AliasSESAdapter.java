package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSESEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAliasImpl;
import com.knubisoft.testlum.testing.framework.db.ses.SESOperation;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.SES;

@Conditional({OnSESEnabledCondition.class})
@Component
public class AliasSESAdapter implements AliasAdapter {

    @Autowired(required = false)
    private SESOperation sesOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Ses ses : ConfigProvider.getDefaultIntegrations().getSesIntegration().getSes()) {
            if (ses.isEnabled()) {
                aliasMap.put(SES + UNDERSCORE + ses.getAlias(), getMetadataSES(ses));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataSES(final Ses ses) {
        return NameToAdapterAliasImpl.Metadata.builder()
                .configuration(ses)
                .storageOperation(sesOperation)
                .build();
    }
}
