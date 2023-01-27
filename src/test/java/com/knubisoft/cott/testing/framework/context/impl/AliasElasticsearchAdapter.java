package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.cott.testing.model.global_config.Elasticsearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Conditional({OnElasticEnabledCondition.class})
@Component
public class AliasElasticsearchAdapter implements AliasAdapter {

    @Autowired(required = false)
    private ElasticsearchOperation elasticsearchOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(
                        s, integrations.getElasticsearchIntegration().getElasticsearch(), aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Elasticsearch> elasticsearchList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Elasticsearch elasticsearch : elasticsearchList) {
            if (elasticsearch.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + elasticsearch.getAlias(), getMetadataElasticsearch(elasticsearch));
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
