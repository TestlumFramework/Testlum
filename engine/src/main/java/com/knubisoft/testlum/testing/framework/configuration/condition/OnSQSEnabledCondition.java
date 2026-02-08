package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.SqsIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSQSEnabledCondition implements Condition {

    private final SqsIntegration sqsIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getSqsIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(sqsIntegration)) {
            return IntegrationsUtil.isEnabled(sqsIntegration.getSqs());
        }
        return false;
    }
}
