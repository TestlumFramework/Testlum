package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.KafkaIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnKafkaEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        GlobalTestConfigurationProvider configurationProvider =
                context.getBeanFactory().getBean(GlobalTestConfigurationProvider.class);
        KafkaIntegration kafkaIntegration =
                configurationProvider.getDefaultIntegrations().getKafkaIntegration();
        if (Objects.nonNull(kafkaIntegration)) {
            return IntegrationsUtil.isEnabled(kafkaIntegration.getKafka());
        }
        return false;
    }
}
