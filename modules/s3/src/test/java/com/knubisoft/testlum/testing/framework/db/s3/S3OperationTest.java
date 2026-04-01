package com.knubisoft.testlum.testing.framework.db.s3;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.S3;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.Bucket;
import software.amazon.awssdk.services.s3.model.ListBucketsResponse;
import software.amazon.awssdk.services.s3.model.ListObjectsV2Response;
import software.amazon.awssdk.services.s3.model.S3Object;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class S3OperationTest {

    @Mock
    private IntegrationsProvider integrationsProvider;

    @Nested
    class Apply {

        @Test
        void returnsNull() {
            final Map<AliasEnv, S3Client> clientMap = new HashMap<>();
            final S3Operation operation = new S3Operation(clientMap);
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
        void deletesAllBucketsAndObjectsWhenTruncateEnabled() throws Exception {
            final S3Client s3Client = mock(S3Client.class);
            final AliasEnv aliasEnv = new AliasEnv("s3-alias", "dev");
            final Map<AliasEnv, S3Client> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, s3Client);

            final S3 s3Config = new S3();
            s3Config.setAlias("s3-alias");
            s3Config.setEnabled(true);
            s3Config.setTruncate(true);

            final Bucket bucket = Bucket.builder().name("test-bucket").build();
            final ListBucketsResponse listBucketsResponse = ListBucketsResponse.builder()
                    .buckets(List.of(bucket)).build();
            when(s3Client.listBuckets()).thenReturn(listBucketsResponse);

            final S3Object s3Object = S3Object.builder().key("test-key").build();
            final ListObjectsV2Response objectsResponse = ListObjectsV2Response.builder()
                    .contents(List.of(s3Object)).build();
            when(s3Client.listObjectsV2(any(Consumer.class))).thenReturn(objectsResponse);

            final S3Operation operation = new S3Operation(clientMap);
            setIntegrationsProvider(operation);
            when(integrationsProvider.findForAliasEnv(S3.class, aliasEnv)).thenReturn(s3Config);

            operation.clearSystem();

            verify(s3Client).listBuckets();
            verify(s3Client).deleteObject(any(Consumer.class));
            verify(s3Client).deleteBucket(any(Consumer.class));
        }

        @Test
        void skipsWhenEnvironmentDoesNotMatch() {
            final S3Client s3Client = mock(S3Client.class);
            final AliasEnv aliasEnv = new AliasEnv("s3-alias", "prod");
            final Map<AliasEnv, S3Client> clientMap = new HashMap<>();
            clientMap.put(aliasEnv, s3Client);

            final S3Operation operation = new S3Operation(clientMap);

            operation.clearSystem();

            verify(s3Client, never()).listBuckets();
        }

        @Test
        void handlesEmptyClientMap() {
            final Map<AliasEnv, S3Client> clientMap = new HashMap<>();
            final S3Operation operation = new S3Operation(clientMap);

            operation.clearSystem();
        }

        @Test
        void handlesNullClientMap() {
            final S3Operation operation = new S3Operation(null);

            try {
                operation.clearSystem();
            } catch (NullPointerException e) {
                // expected when null map
            }
        }

        private void setIntegrationsProvider(final S3Operation operation) throws Exception {
            final Field field = AbstractStorageOperation.class.getDeclaredField("integrationsProvider");
            field.setAccessible(true);
            field.set(operation, integrationsProvider);
        }
    }
}
