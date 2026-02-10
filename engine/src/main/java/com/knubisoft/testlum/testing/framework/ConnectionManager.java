package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import lombok.SneakyThrows;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

import static java.util.Objects.nonNull;

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

    @SneakyThrows
    public void closeConnections() {
        closeRabbitmqConnections();
        closeElasticsearchConnections();
    }

    private void closeRabbitmqConnections() {
        if (nonNull(rabbitConnectionFactoryMap)) {
            rabbitConnectionFactoryMap.values().forEach(connectionFactory ->
                    ((CachingConnectionFactory) connectionFactory).resetConnection());
        }
    }

    private void closeElasticsearchConnections() throws IOException {
        if (nonNull(elasticRestClientMap)) {
            for (RestClient restClient : elasticRestClientMap.values()) {
                restClient.close();
            }
        }
        if (nonNull(elasticRestHighLevelClientMap)) {
            for (RestHighLevelClient restClient : elasticRestHighLevelClientMap.values()) {
                restClient.close();
            }
        }
    }
}
