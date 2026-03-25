package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSendgridEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sendgrid.SendGridOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Sendgrid;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnSendgridEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasSendGridAdapter implements AliasAdapter {

    private final SendGridOperation sendGridOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Sendgrid sendgrid : integrations.getSendgridIntegration().getSendgrid()) {
            if (sendgrid.isEnabled()) {
                aliasMap.put("Sendgrid" + DelimiterConstant.UNDERSCORE + sendgrid.getAlias(), sendGridOperation);
            }
        }
    }
}
