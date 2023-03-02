package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.TwilioIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnTwilioEnabledCondition implements Condition {

    private final TwilioIntegration twilioIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getTwilioIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(twilioIntegration)) {
            return ConfigUtil.isIntegrationEnabled(twilioIntegration.getTwilio());
        }
        return false;
    }
}
