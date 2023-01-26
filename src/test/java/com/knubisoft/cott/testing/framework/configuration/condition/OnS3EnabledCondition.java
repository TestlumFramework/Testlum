package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.S3;
import com.knubisoft.cott.testing.model.global_config.S3Integration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnS3EnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final S3Integration s3Integration = GlobalTestConfigurationProvider.getDefaultIntegration().getS3Integration();
        if (Objects.nonNull(s3Integration)) {
            return s3Integration.getS3()
                    .stream().anyMatch(S3::isEnabled);
        }
        return false;
    }
}
