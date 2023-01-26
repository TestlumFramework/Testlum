package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Sqs;
import com.knubisoft.cott.testing.model.global_config.SqsIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnSQSEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final SqsIntegration sqsIntegration = GlobalTestConfigurationProvider.getDefaultIntegration()
                .getSqsIntegration();
        if (Objects.nonNull(sqsIntegration)) {
            return sqsIntegration.getSqs()
                    .stream().anyMatch(Sqs::isEnabled);
        }
        return false;
    }
}
