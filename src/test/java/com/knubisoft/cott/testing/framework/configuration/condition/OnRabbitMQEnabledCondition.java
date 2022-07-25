package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Rabbitmq;
import com.knubisoft.cott.testing.model.global_config.RabbitmqIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRabbitMQEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final RabbitmqIntegration rabbitmqIntegration =
                GlobalTestConfigurationProvider.getIntegrations().getRabbitmqIntegration();
        if (Objects.nonNull(rabbitmqIntegration)) {
            return rabbitmqIntegration.getRabbitmq().stream().anyMatch(Rabbitmq::isEnabled);
        }
        return false;
    }
}
