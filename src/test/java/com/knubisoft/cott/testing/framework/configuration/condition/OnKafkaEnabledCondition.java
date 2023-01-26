package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Kafka;
import com.knubisoft.cott.testing.model.global_config.KafkaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnKafkaEnabledCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final KafkaIntegration kafkaIntegration =
                GlobalTestConfigurationProvider.getDefaultIntegration().getKafkaIntegration();
        if (Objects.nonNull(kafkaIntegration)) {
            return kafkaIntegration.getKafka()
                    .stream().anyMatch(Kafka::isEnabled);
        }
        return false;
    }
}
