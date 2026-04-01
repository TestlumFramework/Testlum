package com.knubisoft.testlum.testing.framework;

import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConnectionManagerTest {

    @InjectMocks
    private ConnectionManager connectionManager;

    @Test
    void closeConnectionsDoesNotThrowWhenAllMapsAreNull() {
        assertDoesNotThrow(() -> connectionManager.closeConnections());
    }

    @Test
    void closeConnectionsCallsResetConnectionOnRabbitFactories() {
        AliasEnv alias1 = mock(AliasEnv.class);
        AliasEnv alias2 = mock(AliasEnv.class);
        ConnectionFactory factory1 = mock(ConnectionFactory.class);
        ConnectionFactory factory2 = mock(ConnectionFactory.class);

        Map<AliasEnv, ConnectionFactory> rabbitMap = Map.of(alias1, factory1, alias2, factory2);
        ReflectionTestUtils.setField(connectionManager, "rabbitConnectionFactoryMap", rabbitMap);

        connectionManager.closeConnections();

        verify(factory1).resetConnection();
        verify(factory2).resetConnection();
    }

    @Test
    void closeConnectionsCallsCloseOnElasticRestClients() throws IOException {
        AliasEnv alias = mock(AliasEnv.class);
        RestClient restClient = mock(RestClient.class);

        Map<AliasEnv, RestClient> restClientMap = Map.of(alias, restClient);
        ReflectionTestUtils.setField(connectionManager, "elasticRestClientMap", restClientMap);

        connectionManager.closeConnections();

        verify(restClient).close();
    }

    @Test
    void closeConnectionsClosesAllConnectionTypes() throws IOException {
        AliasEnv rabbitAlias = mock(AliasEnv.class);
        AliasEnv restAlias = mock(AliasEnv.class);

        ConnectionFactory factory = mock(ConnectionFactory.class);
        RestClient restClient = mock(RestClient.class);

        ReflectionTestUtils.setField(connectionManager, "rabbitConnectionFactoryMap",
                Map.of(rabbitAlias, factory));
        ReflectionTestUtils.setField(connectionManager, "elasticRestClientMap",
                Map.of(restAlias, restClient));

        connectionManager.closeConnections();

        verify(factory).resetConnection();
        verify(restClient).close();
    }
}
