package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.elasticsearch.ElasticsearchOperation;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.ELASTICSEARCH;

@Conditional({OnElasticEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasElasticsearchAdapter implements AliasAdapter {

    private final ElasticsearchOperation elasticsearchOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Elasticsearch elasticsearch : integrations.getElasticsearchIntegration().getElasticsearch()) {
            if (elasticsearch.isEnabled()) {
                aliasMap.put(ELASTICSEARCH + UNDERSCORE + elasticsearch.getAlias(), elasticsearchOperation);
            }
        }
    }
}
