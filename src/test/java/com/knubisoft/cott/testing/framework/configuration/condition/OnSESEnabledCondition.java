package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.IntegrationsUtil;
import com.knubisoft.cott.testing.model.global_config.SesIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSESEnabledCondition implements Condition {

    private final SesIntegration sesIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getSesIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(sesIntegration)) {
            return IntegrationsUtil.isEnabled(sesIntegration.getSes());
        }
        return false;
    }
}
