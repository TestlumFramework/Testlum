package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.IntegrationsUtil;
import com.knubisoft.cott.testing.model.global_config.LambdaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnLambdaEnabledCondition implements Condition {

    private final LambdaIntegration lambdaIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getLambdaIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(lambdaIntegration)) {
            return IntegrationsUtil.isEnabled(lambdaIntegration.getLambda());
        }
        return false;
    }
}
