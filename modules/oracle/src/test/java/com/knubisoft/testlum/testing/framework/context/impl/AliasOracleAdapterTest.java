package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.OracleOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import com.knubisoft.testlum.testing.model.global_config.OracleIntegration;
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
class AliasOracleAdapterTest {

    @Mock
    private OracleOperation oracleOperation;

    @Mock
    private Integrations integrations;

    private AliasOracleAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AliasOracleAdapter(oracleOperation, integrations);
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsOracleIntegrationList() {
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);
            Oracle oracle = mock(Oracle.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of(oracle));

            List<? extends Integration> result = adapter.getIntegrationList(integrations);

            assertEquals(1, result.size());
            assertSame(oracle, result.get(0));
        }
    }

    @Nested
    class GetStorageName {

        @Test
        void returnsOracleStorageName() {
            String storageName = adapter.getStorageName();
            assertEquals(StorageName.ORACLE.value(), storageName);
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);
            Oracle oracle = mock(Oracle.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of(oracle));
            when(oracle.isEnabled()).thenReturn(true);
            when(oracle.getAlias()).thenReturn("ora1");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            String expectedKey = StorageName.ORACLE.value() + "_" + "ora1";
            assertTrue(aliasMap.containsKey(expectedKey));
            assertSame(oracleOperation, aliasMap.get(expectedKey));
        }

        @Test
        void doesNotAddDisabledIntegration() {
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);
            Oracle oracle = mock(Oracle.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of(oracle));
            when(oracle.isEnabled()).thenReturn(false);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsMultipleEnabledIntegrations() {
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);
            Oracle o1 = mock(Oracle.class);
            Oracle o2 = mock(Oracle.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of(o1, o2));
            when(o1.isEnabled()).thenReturn(true);
            when(o1.getAlias()).thenReturn("alias1");
            when(o2.isEnabled()).thenReturn(true);
            when(o2.getAlias()).thenReturn("alias2");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(2, aliasMap.size());
        }

        @Test
        void handlesEmptyIntegrationList() {
            OracleIntegration oracleIntegration = mock(OracleIntegration.class);

            when(integrations.getOracleIntegration()).thenReturn(oracleIntegration);
            when(oracleIntegration.getOracle()).thenReturn(List.of());

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }
}
