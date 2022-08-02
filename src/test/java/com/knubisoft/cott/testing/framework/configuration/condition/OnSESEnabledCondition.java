package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Ses;
import com.knubisoft.cott.testing.model.global_config.SesIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSESEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final SesIntegration sesIntegration = GlobalTestConfigurationProvider.getIntegrations().getSesIntegration();
        if (Objects.nonNull(sesIntegration)) {
            return sesIntegration.getSes()
                    .stream().anyMatch(Ses::isEnabled);
        }
        return false;
    }
}
