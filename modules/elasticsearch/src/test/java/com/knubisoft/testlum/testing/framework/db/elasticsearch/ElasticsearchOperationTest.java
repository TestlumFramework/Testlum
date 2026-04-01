package com.knubisoft.testlum.testing.framework.db.elasticsearch;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.Elasticsearch;
import org.elasticsearch.client.RestClient;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** Unit tests for {@link ElasticsearchOperation}. */
@ExtendWith(MockitoExtension.class)
class ElasticsearchOperationTest {

    @Mock
    private RestClient restClient;

    @Mock
    private IntegrationsProvider integrationsProvider;

    private ElasticsearchOperation operation;

    @BeforeEach
    void setUp() throws Exception {
        Map<AliasEnv, RestClient> restClientMap = new HashMap<>();
        restClientMap.put(new AliasEnv("es-alias", "dev"), restClient);
        operation = new ElasticsearchOperation(restClientMap);

        Field providerField = operation.getClass().getSuperclass()
                .getDeclaredField("integrationsProvider");
        providerField.setAccessible(true);
        providerField.set(operation, integrationsProvider);
    }

    @AfterEach
    void tearDown() {
        EnvManager.clearCurrentEnv();
    }

    @Nested
    class ApplyMethod {
        @Test
        void returnsNull() {
            Source source = mock(Source.class);
            StorageOperationResult result = operation.apply(source, "es-alias");
            assertNull(result);
        }
    }

    @Nested
    class ClearSystem {
        @Test
        void performsDeleteRequestWhenTruncateEnabledAndEnvMatches() throws IOException {
            EnvManager.setCurrentEnv("dev");

            Elasticsearch esConfig = mock(Elasticsearch.class);
            when(esConfig.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(Elasticsearch.class), any(AliasEnv.class)))
                    .thenReturn(esConfig);

            operation.clearSystem();

            verify(restClient).performRequest(any());
        }

        @Test
        void skipsDeleteWhenTruncateDisabled() throws IOException {
            EnvManager.setCurrentEnv("dev");

            Elasticsearch esConfig = mock(Elasticsearch.class);
            when(esConfig.isTruncate()).thenReturn(false);
            when(integrationsProvider.findForAliasEnv(eq(Elasticsearch.class), any(AliasEnv.class)))
                    .thenReturn(esConfig);

            operation.clearSystem();

            verify(restClient, never()).performRequest(any());
        }

        @Test
        void skipsDeleteWhenEnvironmentDoesNotMatch() {
            EnvManager.setCurrentEnv("prod");

            Elasticsearch esConfig = mock(Elasticsearch.class);
            when(esConfig.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(Elasticsearch.class), any(AliasEnv.class)))
                    .thenReturn(esConfig);

            operation.clearSystem();

            verifyNoInteractions(restClient);
        }

        @Test
        void wrapsIOExceptionInDefaultFrameworkException() throws IOException {
            EnvManager.setCurrentEnv("dev");

            Elasticsearch esConfig = mock(Elasticsearch.class);
            when(esConfig.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(Elasticsearch.class), any(AliasEnv.class)))
                    .thenReturn(esConfig);

            when(restClient.performRequest(any())).thenThrow(new IOException("connection refused"));

            assertThrows(DefaultFrameworkException.class, () -> operation.clearSystem());
        }
    }

    @Nested
    class NullRestClientMap {
        @Test
        void constructorAcceptsNullMap() {
            assertDoesNotThrow(() -> new ElasticsearchOperation(null));
        }
    }
}
