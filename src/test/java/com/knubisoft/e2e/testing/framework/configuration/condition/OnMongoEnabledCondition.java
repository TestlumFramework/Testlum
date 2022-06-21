package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Mongo;
import com.knubisoft.e2e.testing.model.global_config.MongoIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMongoEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final MongoIntegration mongoIntegration =
                GlobalTestConfigurationProvider.getIntegrations().getMongoIntegration();
        if (Objects.nonNull(mongoIntegration)) {
            return mongoIntegration.getMongo()
                    .stream().anyMatch(Mongo::isEnabled);
        }
        return false;
    }
}
