package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRabbitMQEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        RabbitmqIntegration rabbitmqIntegration = ConfigProvider.getDefaultIntegrations().getRabbitmqIntegration();
        if (Objects.nonNull(rabbitmqIntegration)) {
            return IntegrationsUtil.isEnabled(rabbitmqIntegration.getRabbitmq());
        }
        return false;
    }
}
