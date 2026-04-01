package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import com.knubisoft.testlum.testing.model.global_config.PostgresIntegration;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AliasPostgresAdapterTest {

    @Mock
    private PostgresSqlOperation postgresSqlOperation;

    @Mock
    private Integrations integrations;

    private AliasPostgresAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AliasPostgresAdapter(postgresSqlOperation, integrations);
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsPostgresIntegrationList() {
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);
            Postgres postgres = mock(Postgres.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of(postgres));

            List<? extends Integration> result = adapter.getIntegrationList(integrations);

            assertEquals(1, result.size());
            assertSame(postgres, result.get(0));
        }
    }

    @Nested
    class GetStorageName {

        @Test
        void returnsPostgresStorageName() {
            String storageName = adapter.getStorageName();
            assertEquals(StorageName.POSTGRES.value(), storageName);
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);
            Postgres postgres = mock(Postgres.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of(postgres));
            when(postgres.isEnabled()).thenReturn(true);
            when(postgres.getAlias()).thenReturn("pg1");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            String expectedKey = StorageName.POSTGRES.value() + "_" + "pg1";
            assertTrue(aliasMap.containsKey(expectedKey));
            assertSame(postgresSqlOperation, aliasMap.get(expectedKey));
        }

        @Test
        void doesNotAddDisabledIntegration() {
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);
            Postgres postgres = mock(Postgres.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of(postgres));
            when(postgres.isEnabled()).thenReturn(false);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsMultipleEnabledIntegrations() {
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);
            Postgres p1 = mock(Postgres.class);
            Postgres p2 = mock(Postgres.class);
            Postgres p3 = mock(Postgres.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of(p1, p2, p3));
            when(p1.isEnabled()).thenReturn(true);
            when(p1.getAlias()).thenReturn("pg1");
            when(p2.isEnabled()).thenReturn(true);
            when(p2.getAlias()).thenReturn("pg2");
            when(p3.isEnabled()).thenReturn(true);
            when(p3.getAlias()).thenReturn("pg3");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(3, aliasMap.size());
        }

        @Test
        void handlesEmptyIntegrationList() {
            PostgresIntegration postgresIntegration = mock(PostgresIntegration.class);

            when(integrations.getPostgresIntegration()).thenReturn(postgresIntegration);
            when(postgresIntegration.getPostgres()).thenReturn(List.of());

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }
}
