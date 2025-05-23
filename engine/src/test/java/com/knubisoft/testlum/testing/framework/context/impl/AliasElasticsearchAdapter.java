package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.ELASTICSEARCH;

@Conditional({OnElasticEnabledCondition.class})
@Component
public class AliasElasticsearchAdapter implements AliasAdapter {

    @Autowired(required = false)
    private ElasticsearchOperation elasticsearchOperation;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Elasticsearch elasticsearch : GlobalTestConfigurationProvider
                .getDefaultIntegrations().getElasticsearchIntegration().getElasticsearch()) {
            if (elasticsearch.isEnabled()) {
                aliasMap.put(ELASTICSEARCH + UNDERSCORE + elasticsearch.getAlias(), elasticsearchOperation);
            }
        }
    }
}
