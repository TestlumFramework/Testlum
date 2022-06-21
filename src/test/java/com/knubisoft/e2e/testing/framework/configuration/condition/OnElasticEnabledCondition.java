package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Elasticsearch;
import com.knubisoft.e2e.testing.model.global_config.ElasticsearchIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnElasticEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {
        final ElasticsearchIntegration elasticsearchIntegration =
                GlobalTestConfigurationProvider.getIntegrations().getElasticsearchIntegration();
        if (Objects.nonNull(elasticsearchIntegration)) {
            return elasticsearchIntegration.getElasticsearch()
                    .stream().anyMatch(Elasticsearch::isEnabled);
        }
        return false;
    }
}
