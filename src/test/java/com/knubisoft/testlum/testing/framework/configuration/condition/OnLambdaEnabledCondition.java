package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.LambdaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnLambdaEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        GlobalTestConfigurationProvider configurationProvider =
                context.getBeanFactory().getBean(GlobalTestConfigurationProvider.class);
        LambdaIntegration lambdaIntegration =
                configurationProvider.getDefaultIntegrations().getLambdaIntegration();
        if (Objects.nonNull(lambdaIntegration)) {
            return IntegrationsUtil.isEnabled(lambdaIntegration.getLambda());
        }
        return false;
    }
}
