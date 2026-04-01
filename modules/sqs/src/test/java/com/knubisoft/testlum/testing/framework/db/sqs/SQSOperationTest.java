package com.knubisoft.testlum.testing.framework.db.sqs;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Sqs;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.ListQueuesResponse;
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SQSOperationTest {

    @Mock
    private IntegrationsProvider integrationsProvider;

    @Nested
    class Apply {

        @Test
        void returnsNull() {
            final Map<AliasEnv, SqsClient> clientMap = new HashMap<>();
            final SQSOperation operation = new SQSOperation(clientMap);
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
        void purgesAllQueuesWhenTruncateEnabledAndEnvMatches() throws Exception {
            final SqsClient sqsClient = mock(SqsClient.class);
            final AliasEnv aliasEnv = new AliasEnv("sqs-alias", "dev");
            final Map<AliasEnv, SqsClient> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, sqsClient);

            final Sqs sqsConfig = new Sqs();
            sqsConfig.setAlias("sqs-alias");
            sqsConfig.setEnabled(true);
            sqsConfig.setTruncate(true);

            final ListQueuesResponse listQueuesResponse = ListQueuesResponse.builder()
                    .queueUrls(List.of("http://localhost:4566/queue/test-queue")).build();
            when(sqsClient.listQueues()).thenReturn(listQueuesResponse);

            final SQSOperation operation = new SQSOperation(clientMap);
            setIntegrationsProvider(operation);
            when(integrationsProvider.findForAliasEnv(Sqs.class, aliasEnv)).thenReturn(sqsConfig);

            operation.clearSystem();

            verify(sqsClient).purgeQueue(any(PurgeQueueRequest.class));
        }

        @Test
        void purgesMultipleQueues() throws Exception {
            final SqsClient sqsClient = mock(SqsClient.class);
            final AliasEnv aliasEnv = new AliasEnv("sqs-alias", "dev");
            final Map<AliasEnv, SqsClient> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, sqsClient);

            final Sqs sqsConfig = new Sqs();
            sqsConfig.setAlias("sqs-alias");
            sqsConfig.setEnabled(true);
            sqsConfig.setTruncate(true);

            final ListQueuesResponse listQueuesResponse = ListQueuesResponse.builder()
                    .queueUrls(List.of(
                            "http://localhost:4566/queue/queue-1",
                            "http://localhost:4566/queue/queue-2",
                            "http://localhost:4566/queue/queue-3"
                    )).build();
            when(sqsClient.listQueues()).thenReturn(listQueuesResponse);

            final SQSOperation operation = new SQSOperation(clientMap);
            setIntegrationsProvider(operation);
            when(integrationsProvider.findForAliasEnv(Sqs.class, aliasEnv)).thenReturn(sqsConfig);

            operation.clearSystem();

            verify(sqsClient).listQueues();
        }

        @Test
        void skipsWhenEnvironmentDoesNotMatch() {
            final SqsClient sqsClient = mock(SqsClient.class);
            final AliasEnv aliasEnv = new AliasEnv("sqs-alias", "prod");
            final Map<AliasEnv, SqsClient> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, sqsClient);

            final SQSOperation operation = new SQSOperation(clientMap);

            operation.clearSystem();

            verify(sqsClient, never()).listQueues();
        }

        @Test
        void handlesEmptyClientMap() {
            final Map<AliasEnv, SqsClient> clientMap = new HashMap<>();
            final SQSOperation operation = new SQSOperation(clientMap);

            operation.clearSystem();
        }

        @Test
        void handlesEmptyQueueList() throws Exception {
            final SqsClient sqsClient = mock(SqsClient.class);
            final AliasEnv aliasEnv = new AliasEnv("sqs-alias", "dev");
            final Map<AliasEnv, SqsClient> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, sqsClient);

            final Sqs sqsConfig = new Sqs();
            sqsConfig.setAlias("sqs-alias");
            sqsConfig.setEnabled(true);
            sqsConfig.setTruncate(true);

            final ListQueuesResponse emptyResponse = ListQueuesResponse.builder()
                    .queueUrls(List.of()).build();
            when(sqsClient.listQueues()).thenReturn(emptyResponse);

            final SQSOperation operation = new SQSOperation(clientMap);
            setIntegrationsProvider(operation);
            when(integrationsProvider.findForAliasEnv(Sqs.class, aliasEnv)).thenReturn(sqsConfig);

            operation.clearSystem();

            verify(sqsClient, never()).purgeQueue(any(PurgeQueueRequest.class));
        }

        private void setIntegrationsProvider(final SQSOperation operation) throws Exception {
            final Field field = AbstractStorageOperation.class.getDeclaredField("integrationsProvider");
            field.setAccessible(true);
            field.set(operation, integrationsProvider);
        }
    }
}
