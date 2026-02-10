package com.knubisoft.testlum.testing.framework.db.elasticsearch;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Objects;

@Conditional({OnElasticEnabledCondition.class})
@Component
public class ElasticsearchOperation extends AbstractStorageOperation {

    private final Map<AliasEnv, RestHighLevelClient> restHighLevelClient;

    public ElasticsearchOperation(@Autowired(required = false)
                                  final Map<AliasEnv, RestHighLevelClient> restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @SneakyThrows
    @Override
    public void clearSystem() {
        DeleteIndexRequest request = new DeleteIndexRequest("*");
        for (Map.Entry<AliasEnv, RestHighLevelClient> entry : restHighLevelClient.entrySet()) {
            AliasEnv aliasEnv = entry.getKey();
            if (isTruncate(Elasticsearch.class, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                entry.getValue().indices().delete(request, RequestOptions.DEFAULT);
            }
        }
    }
}
