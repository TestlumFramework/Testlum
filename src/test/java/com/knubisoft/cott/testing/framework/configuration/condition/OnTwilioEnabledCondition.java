package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Integration;
import com.knubisoft.cott.testing.model.global_config.TwilioIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnTwilioEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final TwilioIntegration twilioIntegration =
                GlobalTestConfigurationProvider.getIntegrations().getTwilioIntegration();
        if (Objects.nonNull(twilioIntegration)) {
            return twilioIntegration.getTwilio().stream().anyMatch(Integration::isEnabled);
        }
        return false;
    }
}
