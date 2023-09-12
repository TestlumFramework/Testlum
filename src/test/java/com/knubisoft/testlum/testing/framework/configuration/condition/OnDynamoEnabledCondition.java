package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.DynamoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnDynamoEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        DynamoIntegration dynamoIntegration = ConfigProvider.getDefaultIntegrations().getDynamoIntegration();
        if (Objects.nonNull(dynamoIntegration)) {
            return IntegrationsUtil.isEnabled(dynamoIntegration.getDynamo());
        }
        return false;
    }
}
