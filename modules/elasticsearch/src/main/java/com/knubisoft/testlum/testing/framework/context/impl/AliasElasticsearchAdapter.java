package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnElasticEnabledCondition.class})
@Component
public class AliasElasticsearchAdapter extends AbstractAliasAdapter {

    public AliasElasticsearchAdapter(final ElasticsearchOperation elasticsearchOperation,
                                     final Integrations integrations) {
        super(elasticsearchOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getElasticsearchIntegration().getElasticsearch();
    }

    @Override
    protected String getStorageName() {
        return "Elasticsearch";
    }
}
