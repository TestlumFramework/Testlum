package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Component
public class ConnectionManager {

    @Autowired(required = false)
    private Map<AliasEnv, ConnectionFactory> rabbitConnectionFactoryMap;

    @Autowired(required = false)
    @Qualifier("restClient")
    private Map<AliasEnv, RestClient> elasticRestClientMap;

    @Autowired(required = false)
    @Qualifier("restHighLevelClient")
    private Map<AliasEnv, RestHighLevelClient> elasticRestHighLevelClientMap;

    public void closeConnections() {
        closeRabbitmqConnections();
        try {
            closeElasticsearchConnections();
        } catch (IOException e) {
            throw new DefaultFrameworkException(e);
        }
    }

    private void closeRabbitmqConnections() {
        if (Objects.nonNull(rabbitConnectionFactoryMap)) {
            rabbitConnectionFactoryMap.values().forEach(ConnectionFactory::resetConnection);
        }
    }

    private void closeElasticsearchConnections() throws IOException {
        if (Objects.nonNull(elasticRestClientMap)) {
            for (RestClient restClient : elasticRestClientMap.values()) {
                restClient.close();
            }
        }
        if (Objects.nonNull(elasticRestHighLevelClientMap)) {
            for (RestHighLevelClient restClient : elasticRestHighLevelClientMap.values()) {
                restClient.close();
            }
        }
    }
}
