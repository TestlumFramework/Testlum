package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnDynamoEnabledCondition implements Condition {

    private final DynamoIntegration dynamoIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getDynamoIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(dynamoIntegration)) {
            return IntegrationsUtil.isEnabled(dynamoIntegration.getDynamo());
        }
        return false;
    }
}
