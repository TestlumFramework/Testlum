package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Auth;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnAuthEnabledCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Auth auth =
                GlobalTestConfigurationProvider.provide().getAuth();
        return Objects.nonNull(auth);
    }
}
