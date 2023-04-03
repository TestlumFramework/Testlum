package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.IntegrationsUtil;
import com.knubisoft.cott.testing.model.global_config.KafkaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnKafkaEnabledCondition implements Condition {

    private final KafkaIntegration kafkaIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getKafkaIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(kafkaIntegration)) {
            return IntegrationsUtil.isEnabled(kafkaIntegration.getKafka());
        }
        return false;
    }
}
