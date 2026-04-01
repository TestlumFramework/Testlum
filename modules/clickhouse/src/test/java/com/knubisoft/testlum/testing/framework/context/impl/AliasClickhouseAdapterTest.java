package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
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
class AliasClickhouseAdapterTest {

    @Mock
    private ClickhouseOperation clickhouseOperation;

    @Mock
    private Integrations integrations;

    private AliasClickhouseAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AliasClickhouseAdapter(clickhouseOperation, integrations);
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsClickhouseIntegrationList() {
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse clickhouse = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(clickhouse));

            List<? extends Integration> result = adapter.getIntegrationList(integrations);

            assertEquals(1, result.size());
            assertSame(clickhouse, result.get(0));
        }
    }

    @Nested
    class GetStorageName {

        @Test
        void returnsClickhouseStorageName() {
            String storageName = adapter.getStorageName();
            assertEquals(StorageName.CLICKHOUSE.value(), storageName);
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse clickhouse = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(clickhouse));
            when(clickhouse.isEnabled()).thenReturn(true);
            when(clickhouse.getAlias()).thenReturn("ch1");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            String expectedKey = StorageName.CLICKHOUSE.value() + "_" + "ch1";
            assertTrue(aliasMap.containsKey(expectedKey));
            assertSame(clickhouseOperation, aliasMap.get(expectedKey));
        }

        @Test
        void doesNotAddDisabledIntegration() {
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse clickhouse = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(clickhouse));
            when(clickhouse.isEnabled()).thenReturn(false);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void addsMultipleEnabledIntegrations() {
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse ch1 = mock(Clickhouse.class);
            Clickhouse ch2 = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(ch1, ch2));
            when(ch1.isEnabled()).thenReturn(true);
            when(ch1.getAlias()).thenReturn("alias1");
            when(ch2.isEnabled()).thenReturn(true);
            when(ch2.getAlias()).thenReturn("alias2");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(2, aliasMap.size());
        }

        @Test
        void mixedEnabledDisabledIntegrations() {
            ClickhouseIntegration clickhouseIntegration = mock(ClickhouseIntegration.class);
            Clickhouse enabled = mock(Clickhouse.class);
            Clickhouse disabled = mock(Clickhouse.class);

            when(integrations.getClickhouseIntegration()).thenReturn(clickhouseIntegration);
            when(clickhouseIntegration.getClickhouse()).thenReturn(List.of(enabled, disabled));
            when(enabled.isEnabled()).thenReturn(true);
            when(enabled.getAlias()).thenReturn("active");
            when(disabled.isEnabled()).thenReturn(false);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertEquals(1, aliasMap.size());
        }
    }
}
