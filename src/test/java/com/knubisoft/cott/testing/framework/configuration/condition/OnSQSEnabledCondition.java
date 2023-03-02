package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.SqsIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSQSEnabledCondition implements Condition {

    private final SqsIntegration sqsIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getSqsIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(sqsIntegration)) {
            return ConfigUtil.isIntegrationEnabled(sqsIntegration.getSqs());
        }
        return false;
    }
}
