package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.S3;
import com.knubisoft.e2e.testing.model.global_config.S3S;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnS3EnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final S3S s3S = GlobalTestConfigurationProvider.getIntegrations().getS3S();
        if (Objects.nonNull(s3S)) {
            return s3S.getS3()
                    .stream().anyMatch(S3::isEnabled);
        }
        return false;
    }
}
