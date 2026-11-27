package com.testlum.testing.framework.condition;

import com.testlum.testing.model.global_config.Elasticsearch;
import com.testlum.testing.model.global_config.ElasticsearchIntegration;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnElasticEnabledCondition extends AbstractCondition<Elasticsearch> {

    @Override
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations
                .map(Integrations::getElasticsearchIntegration)
                .map(ElasticsearchIntegration::getElasticsearch);
    }
}
