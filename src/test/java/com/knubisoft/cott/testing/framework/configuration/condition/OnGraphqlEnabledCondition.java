package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Graphql;
import com.knubisoft.cott.testing.model.global_config.GraphqlIntegration;
import java.util.Objects;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class OnGraphqlEnabledCondition implements Condition {
    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        final GraphqlIntegration graphqlIntegration = GlobalTestConfigurationProvider.getIntegrations()
                .getGraphqlIntegration();
        if (Objects.nonNull(graphqlIntegration)) {
            return graphqlIntegration.getGraphql().stream().anyMatch(Graphql::isEnabled);
        }
        return false;
    }
}
