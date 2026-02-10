package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnLambdaEnabledCondition implements Condition {

    private final LambdaIntegration lambdaIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getLambdaIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(lambdaIntegration)) {
            return IntegrationsUtil.isEnabled(lambdaIntegration.getLambda());
        }
        return false;
    }
}
