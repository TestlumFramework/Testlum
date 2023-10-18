package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnRabbitMQEnabledCondition implements Condition {

    private final RabbitmqIntegration rabbitmqIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getRabbitmqIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(rabbitmqIntegration)) {
            return IntegrationsUtil.isEnabled(rabbitmqIntegration.getRabbitmq());
        }
        return false;
    }
}
