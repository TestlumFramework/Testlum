package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Sendgrid;
import com.knubisoft.e2e.testing.model.global_config.SendgridIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSendgridEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final SendgridIntegration sendgridIntegration =
                GlobalTestConfigurationProvider.getIntegrations().getSendgridIntegration();
        if (Objects.nonNull(sendgridIntegration)) {
            return sendgridIntegration.getSendgrid()
                    .stream().anyMatch(Sendgrid::isEnabled);
        }
        return false;
    }
}
