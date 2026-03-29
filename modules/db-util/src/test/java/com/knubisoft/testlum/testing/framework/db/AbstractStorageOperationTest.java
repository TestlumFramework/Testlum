package com.knubisoft.testlum.testing.framework.db;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.QueryResult;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.StorageIntegration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
abstract class AbstractStorageOperationTest {

    @Mock
    private IntegrationsProvider integrationsProvider;

    private TestStorageOperation operation;

    @BeforeEach
    void setUp() throws Exception {
        operation = new TestStorageOperation();
        Field field = AbstractStorageOperation.class.getDeclaredField("integrationsProvider");
        field.setAccessible(true);
        field.set(operation, integrationsProvider);
    }

    @Nested
    class ApplyBatch {
        @Test
        void applyMultipleSourcesReturnsResultForEach() {
            Source source1 = new ListSource(List.of("SELECT 1"));
            Source source2 = new ListSource(List.of("SELECT 2"));
            List<StorageOperationResult> results = operation.apply(List.of(source1, source2), "db1");
            assertEquals(2, results.size());
        }

        @Test
        void applyEmptySourceListReturnsEmptyResults() {
            List<StorageOperationResult> results = operation.apply(Collections.emptyList(), "db1");
            assertTrue(results.isEmpty());
        }

        @Test
        void applySingleSourceReturnsOneResult() {
            Source source = new ListSource(List.of("INSERT INTO t VALUES(1)"));
            List<StorageOperationResult> results = operation.apply(List.of(source), "alias");
            assertEquals(1, results.size());
            assertNotNull(results.get(0).getRaw());
        }
    }

    @Nested
    class IsTruncate {
        @Test
        void returnsTrueWhenIntegrationTruncateIsTrue() {
            AliasEnv aliasEnv = mock(AliasEnv.class);
            StorageIntegration integration = mock(StorageIntegration.class);
            when(integration.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(StorageIntegration.class, aliasEnv))
                    .thenReturn(integration);
            assertTrue(operation.isTruncate(StorageIntegration.class, aliasEnv));
        }

        @Test
        void returnsFalseWhenIntegrationTruncateIsFalse() {
            AliasEnv aliasEnv = mock(AliasEnv.class);
            StorageIntegration integration = mock(StorageIntegration.class);
            when(integration.isTruncate()).thenReturn(false);
            when(integrationsProvider.findForAliasEnv(StorageIntegration.class, aliasEnv))
                    .thenReturn(integration);
            assertFalse(operation.isTruncate(StorageIntegration.class, aliasEnv));
        }
    }

    @Nested
    class StorageOperationResultTest {
        @Test
        void rawValueStored() {
            Object raw = List.of("data");
            StorageOperationResult result = new StorageOperationResult(raw);
            assertEquals(raw, result.getRaw());
        }

        @Test
        void rawCanBeNull() {
            StorageOperationResult result = new StorageOperationResult(null);
            assertNull(result.getRaw());
        }

        @Test
        void rawCanBeString() {
            StorageOperationResult result = new StorageOperationResult("result");
            assertEquals("result", result.getRaw());
        }
    }

    @Nested
    class QueryResultTest {
        @Test
        void queryStoredCorrectly() {
            QueryResult<String> result = new QueryResult<>("SELECT 1");
            assertEquals("SELECT 1", result.getQuery());
        }

        @Test
        void contentNullByDefault() {
            QueryResult<Object> result = new QueryResult<>("query");
            assertNull(result.getContent());
        }

        @Test
        void contentCanBeSet() {
            QueryResult<String> result = new QueryResult<>("query");
            result.setContent("result");
            assertEquals("result", result.getContent());
        }

        @Test
        void allArgsConstructorSetsContent() {
            QueryResult<Integer> result = new QueryResult<>("query", 42);
            assertEquals("query", result.getQuery());
            assertEquals(42, result.getContent());
        }
    }

    private static class TestStorageOperation extends AbstractStorageOperation {
        @Override
        public StorageOperationResult apply(final Source source, final String databaseAlias) {
            return new StorageOperationResult(source.getQueries());
        }

        @Override
        public void clearSystem() {
            // no-op for testing
        }
    }
}
