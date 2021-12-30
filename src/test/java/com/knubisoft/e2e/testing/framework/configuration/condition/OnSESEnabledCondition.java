package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Ses;
import com.knubisoft.e2e.testing.model.global_config.Seses;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSESEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Seses seses = GlobalTestConfigurationProvider.provide().getSeses();
        if (Objects.nonNull(seses)) {
            return seses.getSes()
                    .stream().anyMatch(Ses::isEnabled);
        }
        return false;
    }
}
