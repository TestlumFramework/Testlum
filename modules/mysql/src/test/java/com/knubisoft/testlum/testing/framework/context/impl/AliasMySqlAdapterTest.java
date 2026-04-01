package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
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
class AliasMySqlAdapterTest {

    @Mock
    private MySqlOperation mySqlOperation;

    @Mock
    private Integrations integrations;

    private AliasMySqlAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new AliasMySqlAdapter(mySqlOperation, integrations);
    }

    @Nested
    class GetIntegrationList {

        @Test
        void returnsMysqlIntegrationList() {
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);
            Mysql mysql = mock(Mysql.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(List.of(mysql));

            List<? extends Integration> result = adapter.getIntegrationList(integrations);

            assertEquals(1, result.size());
            assertSame(mysql, result.get(0));
        }
    }

    @Nested
    class GetStorageName {

        @Test
        void returnsMysqlStorageName() {
            String storageName = adapter.getStorageName();
            assertEquals(StorageName.MYSQL.value(), storageName);
        }
    }

    @Nested
    class Apply {

        @Test
        void addsEnabledIntegrationToAliasMap() {
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);
            Mysql mysql = mock(Mysql.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(List.of(mysql));
            when(mysql.isEnabled()).thenReturn(true);
            when(mysql.getAlias()).thenReturn("mysql1");

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            String expectedKey = StorageName.MYSQL.value() + "_" + "mysql1";
            assertTrue(aliasMap.containsKey(expectedKey));
            assertSame(mySqlOperation, aliasMap.get(expectedKey));
        }

        @Test
        void doesNotAddDisabledIntegration() {
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);
            Mysql mysql = mock(Mysql.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(List.of(mysql));
            when(mysql.isEnabled()).thenReturn(false);

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }

        @Test
        void handlesEmptyIntegrationList() {
            MysqlIntegration mysqlIntegration = mock(MysqlIntegration.class);

            when(integrations.getMysqlIntegration()).thenReturn(mysqlIntegration);
            when(mysqlIntegration.getMysql()).thenReturn(List.of());

            Map<String, AbstractStorageOperation> aliasMap = new HashMap<>();
            adapter.apply(aliasMap);

            assertTrue(aliasMap.isEmpty());
        }
    }
}
