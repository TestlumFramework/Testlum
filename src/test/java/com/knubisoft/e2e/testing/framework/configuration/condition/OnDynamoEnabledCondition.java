package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Dynamo;
import com.knubisoft.e2e.testing.model.global_config.DynamoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnDynamoEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final DynamoIntegration dynamoIntegration =
                GlobalTestConfigurationProvider.getIntegrations().getDynamoIntegration();
        if (Objects.nonNull(dynamoIntegration)) {
            return dynamoIntegration.getDynamo()
                    .stream().anyMatch(Dynamo::isEnabled);
        }
        return false;
    }
}
