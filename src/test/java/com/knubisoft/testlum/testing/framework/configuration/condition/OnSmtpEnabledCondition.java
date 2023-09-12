package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.SmtpIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSmtpEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        SmtpIntegration smtpIntegration = ConfigProvider.getDefaultIntegrations().getSmtpIntegration();
        if (Objects.nonNull(smtpIntegration)) {
            return IntegrationsUtil.isEnabled(smtpIntegration.getSmtp());
        }
        return false;
    }
}
