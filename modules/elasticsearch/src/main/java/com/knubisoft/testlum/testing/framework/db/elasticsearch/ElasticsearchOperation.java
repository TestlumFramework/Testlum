package com.knubisoft.testlum.testing.framework.db.elasticsearch;

import com.knubisoft.testlum.testing.framework.condition.OnElasticEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Storage operation implementation for Elasticsearch that handles index cleanup and query execution.
 */
@Conditional({OnElasticEnabledCondition.class})
@Component
public class ElasticsearchOperation extends AbstractStorageOperation {

    private final Map<AliasEnv, RestClient> restClient;

    public ElasticsearchOperation(@Autowired(required = false) @Qualifier("restClient")
                                  final Map<AliasEnv, RestClient> restClient) {
        this.restClient = restClient;
    }

    @Override
    public StorageOperationResult apply(final Source source, final String alias) {
        return null;
    }

    @Override
    public void clearSystem() {
        Request request = new Request("DELETE", "/_all");
        for (Map.Entry<AliasEnv, RestClient> entry : restClient.entrySet()) {
            AliasEnv aliasEnv = entry.getKey();
            if (isTruncate(Elasticsearch.class, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                try {
                    entry.getValue().performRequest(request);
                } catch (IOException e) {
                    throw new DefaultFrameworkException(e);
                }
            }
        }
    }
}
