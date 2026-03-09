package com.knubisoft.testlum.testing.framework.condition;

import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnElasticEnabledCondition extends AbstractCondition<Elasticsearch> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getElasticsearchIntegration())
                .map(ElasticsearchIntegration::getElasticsearch)
                .orElse(null);
    }
}
