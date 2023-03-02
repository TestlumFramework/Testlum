package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.LambdaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnLambdaEnabledCondition implements Condition {

    private final LambdaIntegration lambdaIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getLambdaIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(lambdaIntegration)) {
            return ConfigUtil.isIntegrationEnabled(lambdaIntegration.getLambda());
        }
        return false;
    }
}
