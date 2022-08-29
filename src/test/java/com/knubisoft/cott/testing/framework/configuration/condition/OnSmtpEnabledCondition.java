package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.SmtpIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSmtpEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final SmtpIntegration smtpIntegration = GlobalTestConfigurationProvider.getIntegrations().getSmtpIntegration();
        if (Objects.nonNull(smtpIntegration)) {
            return smtpIntegration.getSmtp().stream().anyMatch(Integration::isEnabled);
        }
        return false;
    }
}
