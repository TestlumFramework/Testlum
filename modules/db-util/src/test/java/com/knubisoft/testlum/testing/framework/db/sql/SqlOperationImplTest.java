package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.QueryResult;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.StorageOperationResult;
import com.knubisoft.testlum.testing.framework.db.source.ListSource;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProvider;
import com.knubisoft.testlum.testing.model.global_config.StorageIntegration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SqlOperationImplTest {

    @Mock
    private IntegrationsProvider integrationsProvider;

    @BeforeEach
    void setUp() {
        EnvManager.setCurrentEnv("test");
    }

    @AfterEach
    void tearDown() {
        EnvManager.clearCurrentEnv();
    }

    private void injectIntegrationsProvider(final AbstractSqlOperation operation) {
        try {
            Field field = com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation.class
                    .getDeclaredField("integrationsProvider");
            field.setAccessible(true);
            field.set(operation, integrationsProvider);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Nested
    class ConstructorTest {

        @Test
        void createsExecutorForEachDataSourceEntry() {
            DataSource ds1 = mock(DataSource.class);
            DataSource ds2 = mock(DataSource.class);
            Map<AliasEnv, DataSource> dataSources = new HashMap<>();
            dataSources.put(new AliasEnv("alias1", "test"), ds1);
            dataSources.put(new AliasEnv("alias2", "test"), ds2);

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class);

            assertEquals(2, operation.getCreateExecutorCallCount());
        }

        @Test
        void emptyDataSourcesCreatesNoExecutors() {
            TestSqlOperation operation = new TestSqlOperation(new HashMap<>(), StorageIntegration.class);

            assertEquals(0, operation.getCreateExecutorCallCount());
        }

        @Test
        void singleDataSourceCreatesOneExecutor() {
            DataSource ds = mock(DataSource.class);
            Map<AliasEnv, DataSource> dataSources = Map.of(new AliasEnv("db", "test"), ds);

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class);

            assertEquals(1, operation.getCreateExecutorCallCount());
        }
    }

    @Nested
    class ApplyTest {

        @Test
        void delegatesToExecutorAndReturnsResult() {
            AbstractSqlExecutor mockExecutor = mock(AbstractSqlExecutor.class);
            List<QueryResult<Object>> queryResults = List.of(new QueryResult<>("SELECT 1", "result"));
            when(mockExecutor.executeQueries(any())).thenReturn(queryResults);

            AliasEnv aliasEnv = new AliasEnv("mydb", "test");
            Map<AliasEnv, DataSource> dataSources = Map.of(aliasEnv, mock(DataSource.class));

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class, mockExecutor);

            Source source = new ListSource(List.of("SELECT 1"));
            StorageOperationResult result = operation.apply(source, "mydb");

            assertNotNull(result);
            assertEquals(queryResults, result.getRaw());
            verify(mockExecutor).executeQueries(List.of("SELECT 1"));
        }

        @Test
        void passesQueriesToExecutor() {
            AbstractSqlExecutor mockExecutor = mock(AbstractSqlExecutor.class);
            List<String> queries = List.of("INSERT INTO t VALUES(1)", "SELECT * FROM t");
            when(mockExecutor.executeQueries(queries)).thenReturn(List.of());

            AliasEnv aliasEnv = new AliasEnv("db", "test");
            Map<AliasEnv, DataSource> dataSources = Map.of(aliasEnv, mock(DataSource.class));

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class, mockExecutor);

            Source source = new ListSource(queries);
            operation.apply(source, "db");

            verify(mockExecutor).executeQueries(queries);
        }
    }

    @Nested
    class ClearSystemTest {

        @Test
        void callsTruncateOnMatchingEnvWhenTruncateIsTrue() {
            AbstractSqlExecutor mockExecutor = mock(AbstractSqlExecutor.class);
            AliasEnv aliasEnv = new AliasEnv("db1", "test");
            Map<AliasEnv, DataSource> dataSources = Map.of(aliasEnv, mock(DataSource.class));

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class, mockExecutor);
            injectIntegrationsProvider(operation);

            StorageIntegration storageIntegration = mock(StorageIntegration.class);
            when(storageIntegration.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(StorageIntegration.class), eq(aliasEnv)))
                    .thenReturn(storageIntegration);

            operation.clearSystem();

            verify(mockExecutor).truncate();
        }

        @Test
        void skipsWhenTruncateIsFalse() {
            AbstractSqlExecutor mockExecutor = mock(AbstractSqlExecutor.class);
            AliasEnv aliasEnv = new AliasEnv("db1", "test");
            Map<AliasEnv, DataSource> dataSources = Map.of(aliasEnv, mock(DataSource.class));

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class, mockExecutor);
            injectIntegrationsProvider(operation);

            StorageIntegration storageIntegration = mock(StorageIntegration.class);
            when(storageIntegration.isTruncate()).thenReturn(false);
            when(integrationsProvider.findForAliasEnv(eq(StorageIntegration.class), eq(aliasEnv)))
                    .thenReturn(storageIntegration);

            operation.clearSystem();

            verify(mockExecutor, never()).truncate();
        }

        @Test
        void skipsWhenEnvDoesNotMatch() {
            AbstractSqlExecutor mockExecutor = mock(AbstractSqlExecutor.class);
            AliasEnv aliasEnv = new AliasEnv("db1", "prod");
            Map<AliasEnv, DataSource> dataSources = Map.of(aliasEnv, mock(DataSource.class));

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class, mockExecutor);
            injectIntegrationsProvider(operation);

            StorageIntegration storageIntegration = mock(StorageIntegration.class);
            when(storageIntegration.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(StorageIntegration.class), eq(aliasEnv)))
                    .thenReturn(storageIntegration);

            operation.clearSystem();

            verify(mockExecutor, never()).truncate();
        }

        @Test
        void truncatesOnlyMatchingEnvExecutors() {
            AbstractSqlExecutor mockExecutor1 = mock(AbstractSqlExecutor.class);
            AliasEnv aliasEnv1 = new AliasEnv("db1", "test");
            AliasEnv aliasEnv2 = new AliasEnv("db2", "prod");

            Map<AliasEnv, DataSource> dataSources = new HashMap<>();
            dataSources.put(aliasEnv1, mock(DataSource.class));
            dataSources.put(aliasEnv2, mock(DataSource.class));

            AbstractSqlExecutor mockExecutor2 = mock(AbstractSqlExecutor.class);
            Map<AliasEnv, AbstractSqlExecutor> executorMap = new HashMap<>();
            executorMap.put(aliasEnv1, mockExecutor1);
            executorMap.put(aliasEnv2, mockExecutor2);

            TestSqlOperation operation = new TestSqlOperation(dataSources, StorageIntegration.class, executorMap);
            injectIntegrationsProvider(operation);

            StorageIntegration integration1 = mock(StorageIntegration.class);
            when(integration1.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(StorageIntegration.class), eq(aliasEnv1)))
                    .thenReturn(integration1);

            StorageIntegration integration2 = mock(StorageIntegration.class);
            when(integration2.isTruncate()).thenReturn(true);
            when(integrationsProvider.findForAliasEnv(eq(StorageIntegration.class), eq(aliasEnv2)))
                    .thenReturn(integration2);

            operation.clearSystem();

            verify(mockExecutor1).truncate();
            verify(mockExecutor2, never()).truncate();
        }
    }

    private static class TestSqlOperation extends AbstractSqlOperation {

        private int createExecutorCallCount;
        private AbstractSqlExecutor fixedExecutor;

        TestSqlOperation(final Map<AliasEnv, DataSource> dataSources,
                         final Class<? extends StorageIntegration> integrationType) {
            super(dataSources, integrationType);
        }

        TestSqlOperation(final Map<AliasEnv, DataSource> dataSources,
                         final Class<? extends StorageIntegration> integrationType,
                         final AbstractSqlExecutor fixedExecutor) {
            super(dataSources, integrationType);
            this.fixedExecutor = fixedExecutor;
            // Replace the executors map via reflection with our mock executor
            replaceExecutors(dataSources, fixedExecutor);
        }

        TestSqlOperation(final Map<AliasEnv, DataSource> dataSources,
                         final Class<? extends StorageIntegration> integrationType,
                         final Map<AliasEnv, AbstractSqlExecutor> executorMap) {
            super(dataSources, integrationType);
            replaceExecutorsMap(executorMap);
        }

        private void replaceExecutors(final Map<AliasEnv, DataSource> dataSources,
                                       final AbstractSqlExecutor executor) {
            try {
                Field executorsField = AbstractSqlOperation.class.getDeclaredField("executors");
                executorsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<AliasEnv, AbstractSqlExecutor> executors =
                        (Map<AliasEnv, AbstractSqlExecutor>) executorsField.get(this);
                executors.clear();
                dataSources.keySet().forEach(key -> executors.put(key, executor));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private void replaceExecutorsMap(final Map<AliasEnv, AbstractSqlExecutor> executorMap) {
            try {
                Field executorsField = AbstractSqlOperation.class.getDeclaredField("executors");
                executorsField.setAccessible(true);
                @SuppressWarnings("unchecked")
                Map<AliasEnv, AbstractSqlExecutor> executors =
                        (Map<AliasEnv, AbstractSqlExecutor>) executorsField.get(this);
                executors.clear();
                executors.putAll(executorMap);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        protected AbstractSqlExecutor createExecutor(final DataSource dataSource) {
            createExecutorCallCount++;
            if (fixedExecutor != null) {
                return fixedExecutor;
            }
            return mock(AbstractSqlExecutor.class);
        }

        int getCreateExecutorCallCount() {
            return createExecutorCallCount;
        }
    }
}
