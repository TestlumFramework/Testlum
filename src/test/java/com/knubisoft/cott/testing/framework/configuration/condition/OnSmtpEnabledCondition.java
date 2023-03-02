package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.SmtpIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSmtpEnabledCondition implements Condition {

    private final SmtpIntegration smtpIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getSmtpIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(smtpIntegration)) {
            return ConfigUtil.isIntegrationEnabled(smtpIntegration.getSmtp());
        }
        return false;
    }
}
