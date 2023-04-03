package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.IntegrationsUtil;
import com.knubisoft.cott.testing.model.global_config.SendgridIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSendgridEnabledCondition implements Condition {

    private final SendgridIntegration sendgridIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getSendgridIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(sendgridIntegration)) {
            return IntegrationsUtil.isEnabled(sendgridIntegration.getSendgrid());
        }
        return false;
    }
}
