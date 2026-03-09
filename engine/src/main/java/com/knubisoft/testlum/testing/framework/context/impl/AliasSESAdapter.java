package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSESEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.ses.SESOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Ses;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.SES;

@Conditional({OnSESEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasSESAdapter implements AliasAdapter {

    private final SESOperation sesOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Ses ses : integrations.getSesIntegration().getSes()) {
            if (ses.isEnabled()) {
                aliasMap.put(SES + UNDERSCORE + ses.getAlias(), sesOperation);
            }
        }
    }
}
