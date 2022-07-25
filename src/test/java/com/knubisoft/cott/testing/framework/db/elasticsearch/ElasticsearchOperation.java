package com.knubisoft.cott.testing.framework.db.elasticsearch;

import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import lombok.SneakyThrows;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class ElasticsearchOperation implements StorageOperation {

    private final Map<String, RestHighLevelClient> restHighLevelClient;

    public ElasticsearchOperation(@Autowired(required = false)
                                  final Map<String, RestHighLevelClient> restHighLevelClient) {
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
        for (Map.Entry<String, RestHighLevelClient> entry : restHighLevelClient.entrySet()) {
            restHighLevelClient.get(entry.getKey()).indices().delete(request, RequestOptions.DEFAULT);
        }
    }
}
