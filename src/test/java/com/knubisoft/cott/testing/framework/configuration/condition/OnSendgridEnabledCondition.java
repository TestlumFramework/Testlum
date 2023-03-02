package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.SendgridIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSendgridEnabledCondition implements Condition {

    private final SendgridIntegration sendgridIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getSendgridIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(sendgridIntegration)) {
            return ConfigUtil.isIntegrationEnabled(sendgridIntegration.getSendgrid());
        }
        return false;
    }
}
