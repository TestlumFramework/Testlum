package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Dynamo;
import com.knubisoft.e2e.testing.model.global_config.Dynamos;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnDynamoEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Dynamos dynamos = GlobalTestConfigurationProvider.getIntegrations().getDynamos();
        if (Objects.nonNull(dynamos)) {
            return dynamos.getDynamo()
                    .stream().anyMatch(Dynamo::isEnabled);
        }
        return false;
    }
}
