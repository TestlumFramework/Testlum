package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Lambda;
import com.knubisoft.cott.testing.model.global_config.LambdaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnLambdaEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final LambdaIntegration lambdaIntegration = GlobalTestConfigurationProvider.getIntegrations()
                .getLambdaIntegration();
        if (Objects.nonNull(lambdaIntegration)) {
            return lambdaIntegration.getLambda().stream().anyMatch(Lambda::isEnabled);
        }
        return false;
    }
}
