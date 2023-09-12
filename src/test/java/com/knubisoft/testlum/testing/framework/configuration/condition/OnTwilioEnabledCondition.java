package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.TwilioIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnTwilioEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        TwilioIntegration twilioIntegration = ConfigProvider.getDefaultIntegrations().getTwilioIntegration();
        if (Objects.nonNull(twilioIntegration)) {
            return IntegrationsUtil.isEnabled(twilioIntegration.getTwilio());
        }
        return false;
    }
}
