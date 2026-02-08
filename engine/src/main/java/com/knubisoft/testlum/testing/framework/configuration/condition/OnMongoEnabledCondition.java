package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.MongoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMongoEnabledCondition implements Condition {

    private final MongoIntegration mongoIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getMongoIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(mongoIntegration)) {
            return IntegrationsUtil.isEnabled(mongoIntegration.getMongo());
        }
        return false;
    }
}
