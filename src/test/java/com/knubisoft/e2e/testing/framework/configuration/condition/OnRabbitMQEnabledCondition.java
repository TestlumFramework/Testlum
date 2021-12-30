package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Rabbitmq;
import com.knubisoft.e2e.testing.model.global_config.Rabbitmqs;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRabbitMQEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Rabbitmqs rabbitmqs = GlobalTestConfigurationProvider.provide().getRabbitmqs();
        if (Objects.nonNull(rabbitmqs)) {
            return rabbitmqs.getRabbitmq().stream().anyMatch(Rabbitmq::isEnabled);
        }
        return false;
    }
}
