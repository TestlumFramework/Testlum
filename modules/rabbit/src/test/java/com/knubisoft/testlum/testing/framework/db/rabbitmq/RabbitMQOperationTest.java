package com.knubisoft.testlum.testing.framework.db.rabbitmq;

import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Rabbitmq;
import com.knubisoft.testlum.testing.model.global_config.RabbitmqIntegration;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RabbitMQOperationTest {

    @Mock
    private IntegrationsProvider integrationsProvider;

    @Mock
    private IntegrationsUtil integrationsUtil;

    @Mock
    private EnvToIntegrationMap envToIntegrations;

    @Nested
    class Apply {

        @Test
        void returnsNull() {
            final Map<AliasEnv, Client> clientMap = new HashMap<>();
            final RabbitMQOperation operation = new RabbitMQOperation(clientMap, envToIntegrations, integrationsUtil);
            final Source source = mock(Source.class);

            final AbstractStorageOperation.StorageOperationResult result = operation.apply(source, "test");

            assertNull(result);
        }
    }

    @Nested
    class ClearSystem {

        @BeforeEach
        void setUp() {
            EnvManager.setCurrentEnv("dev");
        }

        @AfterEach
        void tearDown() {
            EnvManager.clearCurrentEnv();
        }

        @Test
        void purgesQueuesWhenTruncateEnabledAndEnvMatches() throws Exception {
            final Client client = mock(Client.class);
            final AliasEnv aliasEnv = new AliasEnv("rmq1", "dev");
            final Map<AliasEnv, Client> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, client);

            final Rabbitmq rabbitmq = new Rabbitmq();
            rabbitmq.setAlias("rmq1");
            rabbitmq.setEnabled(true);
            rabbitmq.setTruncate(true);
            rabbitmq.setVirtualHost("/");
            final RabbitmqIntegration rmqIntegration = new RabbitmqIntegration();
            rmqIntegration.getRabbitmq().add(rabbitmq);
            final Integrations integrations = new Integrations();
            integrations.setRabbitmqIntegration(rmqIntegration);

            when(envToIntegrations.get("dev")).thenReturn(integrations);
            when(integrationsUtil.findForAlias(rmqIntegration.getRabbitmq(), "rmq1")).thenReturn(rabbitmq);

            final QueueInfo queueInfo = mock(QueueInfo.class);
            when(queueInfo.getName()).thenReturn("test-queue");
            when(client.getQueues()).thenReturn(List.of(queueInfo));

            final RabbitMQOperation operation = new RabbitMQOperation(clientMap, envToIntegrations, integrationsUtil);
            setIntegrationsProvider(operation);
            when(integrationsProvider.findForAliasEnv(Rabbitmq.class, aliasEnv)).thenReturn(rabbitmq);

            operation.clearSystem();

            verify(client).purgeQueue("/", "test-queue");
        }

        @Test
        void skipsWhenEnvironmentDoesNotMatch() throws Exception {
            final Client client = mock(Client.class);
            final AliasEnv aliasEnv = new AliasEnv("rmq1", "prod");
            final Map<AliasEnv, Client> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, client);

            final RabbitMQOperation operation = new RabbitMQOperation(clientMap, envToIntegrations, integrationsUtil);

            operation.clearSystem();

            verify(client, never()).getQueues();
        }

        @Test
        void handlesEmptyClientMap() {
            final Map<AliasEnv, Client> clientMap = new HashMap<>();
            final RabbitMQOperation operation = new RabbitMQOperation(clientMap, envToIntegrations, integrationsUtil);

            operation.clearSystem();
        }

        private void setIntegrationsProvider(final RabbitMQOperation operation) throws Exception {
            final Field field = AbstractStorageOperation.class.getDeclaredField("integrationsProvider");
            field.setAccessible(true);
            field.set(operation, integrationsProvider);
        }
    }
}
