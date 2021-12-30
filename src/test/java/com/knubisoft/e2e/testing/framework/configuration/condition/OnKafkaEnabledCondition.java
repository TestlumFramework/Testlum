package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Kafka;
import com.knubisoft.e2e.testing.model.global_config.Kafkas;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnKafkaEnabledCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final Kafkas kafkas = GlobalTestConfigurationProvider.provide().getKafkas();
        if (Objects.nonNull(kafkas)) {
            return kafkas.getKafka()
                    .stream().anyMatch(Kafka::isEnabled);
        }
        return false;
    }
}
