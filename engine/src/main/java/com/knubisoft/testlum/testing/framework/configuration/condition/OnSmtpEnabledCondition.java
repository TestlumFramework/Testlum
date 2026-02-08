package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.SmtpIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSmtpEnabledCondition implements Condition {

    private final SmtpIntegration smtpIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getSmtpIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(smtpIntegration)) {
            return IntegrationsUtil.isEnabled(smtpIntegration.getSmtp());
        }
        return false;
    }
}
