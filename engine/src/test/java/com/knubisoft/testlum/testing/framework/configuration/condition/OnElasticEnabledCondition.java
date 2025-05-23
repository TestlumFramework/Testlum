package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnElasticEnabledCondition implements Condition {

    private final ElasticsearchIntegration elasticsearchIntegration =
            GlobalTestConfigurationProvider.getDefaultIntegrations().getElasticsearchIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(elasticsearchIntegration)) {
            return IntegrationsUtil.isEnabled(elasticsearchIntegration.getElasticsearch());
        }
        return false;
    }
}
