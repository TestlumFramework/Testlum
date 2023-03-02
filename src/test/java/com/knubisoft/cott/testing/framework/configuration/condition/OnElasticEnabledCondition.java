package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.ElasticsearchIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnElasticEnabledCondition implements Condition {

    private final ElasticsearchIntegration elasticsearchIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegration().getElasticsearchIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(elasticsearchIntegration)) {
            return ConfigUtil.isIntegrationEnabled(elasticsearchIntegration.getElasticsearch());
        }
        return false;
    }
}
