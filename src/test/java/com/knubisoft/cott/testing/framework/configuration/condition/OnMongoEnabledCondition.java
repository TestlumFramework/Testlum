package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.MongoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMongoEnabledCondition implements Condition {

    private final MongoIntegration mongoIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getMongoIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(mongoIntegration)) {
            return ConfigUtil.isIntegrationEnabled(mongoIntegration.getMongo());
        }
        return false;
    }
}
