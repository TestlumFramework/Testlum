package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.e2e.testing.model.global_config.Elasticsearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnElasticEnabledCondition.class})
@Component
public class AliasElasticsearchAdapter implements AliasAdapter {

    @Autowired(required = false)
    private ElasticsearchOperation elasticsearchOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Elasticsearch elasticsearch : GlobalTestConfigurationProvider.provide().getElasticsearches()
                .getElasticsearch()) {
            if (elasticsearch.isEnabled()) {
                aliasMap.put(elasticsearch.getAlias(), getMetadataElasticsearch(elasticsearch));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataElasticsearch(
            final Elasticsearch elasticsearch) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(elasticsearch)
                .storageOperation(elasticsearchOperation)
                .build();
    }
}
