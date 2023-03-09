package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.cott.testing.model.global_config.Elasticsearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.framework.constant.MigrationConstant.ELASTICSEARCH;

@Conditional({OnElasticEnabledCondition.class})
@Component
public class AliasElasticsearchAdapter implements AliasAdapter {

    @Autowired(required = false)
    private ElasticsearchOperation elasticsearchOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Elasticsearch elasticsearch : GlobalTestConfigurationProvider.getDefaultIntegrations()
                .getElasticsearchIntegration().getElasticsearch()) {
            if (elasticsearch.isEnabled()) {
                aliasMap.put(ELASTICSEARCH + UNDERSCORE + elasticsearch.getAlias(),
                        getMetadataElasticsearch(elasticsearch));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataElasticsearch(final Elasticsearch elasticsearch) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(elasticsearch)
                .storageOperation(elasticsearchOperation)
                .build();
    }
}
