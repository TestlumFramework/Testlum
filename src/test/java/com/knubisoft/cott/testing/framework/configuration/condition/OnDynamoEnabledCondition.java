package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.DynamoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnDynamoEnabledCondition implements Condition {

    private final DynamoIntegration dynamoIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getDynamoIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(dynamoIntegration)) {
            return ConfigUtil.isIntegrationEnabled(dynamoIntegration.getDynamo());
        }
        return false;
    }
}
