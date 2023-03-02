package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.S3Integration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnS3EnabledCondition implements Condition {

    private final S3Integration s3Integration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getS3Integration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(s3Integration)) {
            return ConfigUtil.isIntegrationEnabled(s3Integration.getS3());
        }
        return false;
    }
}
