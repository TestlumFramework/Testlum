package com.knubisoft.testlum.testing.framework.util;

import com.knubisoft.testlum.testing.model.global_config.DatabaseConfig;
import com.knubisoft.testlum.testing.model.global_config.Hikari;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataSourceUtilTest {

    private DataSourceUtil dataSourceUtil;

    @BeforeEach
    void setUp() {
        dataSourceUtil = new DataSourceUtil();
    }

    @Nested
    class GetHikariDataSource {
        @Test
        void createsDataSourceWithDefaultSettings() {
            DatabaseConfig config = createDatabaseConfig(null);

            DataSource result = dataSourceUtil.getHikariDataSource(config);

            assertNotNull(result);
            assertInstanceOf(HikariDataSource.class, result);
            HikariDataSource hikari = (HikariDataSource) result;
            assertEquals("jdbc:h2:mem:test", hikari.getJdbcUrl());
            assertEquals("user", hikari.getUsername());
            assertEquals("pass", hikari.getPassword());
        }

        @Test
        void setsHikariPoolSettings() {
            DatabaseConfig config = createDatabaseConfig(null);

            DataSource result = dataSourceUtil.getHikariDataSource(config);

            HikariDataSource hikari = (HikariDataSource) result;
            assertEquals(10, hikari.getMaximumPoolSize());
            assertEquals("testPool", hikari.getPoolName());
            assertEquals(2, hikari.getMinimumIdle());
            assertEquals(30000, hikari.getIdleTimeout());
            assertEquals(60000, hikari.getMaxLifetime());
            assertTrue(hikari.isAutoCommit());
            assertEquals("SELECT 1", hikari.getConnectionTestQuery());
        }

        @Test
        void setsSchemaWhenProvided() {
            DatabaseConfig config = createDatabaseConfig("public");

            DataSource result = dataSourceUtil.getHikariDataSource(config);

            HikariDataSource hikari = (HikariDataSource) result;
            assertEquals("public", hikari.getSchema());
            assertEquals("public", hikari.getCatalog());
        }

        @Test
        void doesNotSetSchemaWhenBlank() {
            DatabaseConfig config = createDatabaseConfig("");

            DataSource result = dataSourceUtil.getHikariDataSource(config);

            HikariDataSource hikari = (HikariDataSource) result;
            assertNull(hikari.getSchema());
        }

        @Test
        void doesNotSetSchemaWhenNull() {
            DatabaseConfig config = createDatabaseConfig(null);

            DataSource result = dataSourceUtil.getHikariDataSource(config);

            HikariDataSource hikari = (HikariDataSource) result;
            assertNull(hikari.getSchema());
        }
    }

    private DatabaseConfig createDatabaseConfig(final String schema) {
        Hikari hikari = new Hikari();
        hikari.setMaximumPoolSize(10);
        hikari.setAutoCommit(true);
        hikari.setConnectionInitSql(null);
        hikari.setConnectionTestQuery("SELECT 1");
        hikari.setPoolName("testPool");
        hikari.setIdleTimeout(30000);
        hikari.setMinimumIdle(2);
        hikari.setMaxLifetime(60000);

        DatabaseConfig config = mock(DatabaseConfig.class);
        when(config.getJdbcDriver()).thenReturn(null);
        when(config.getConnectionUrl()).thenReturn("jdbc:h2:mem:test");
        when(config.getUsername()).thenReturn("user");
        when(config.getPassword()).thenReturn("pass");
        when(config.getSchema()).thenReturn(schema);
        when(config.getHikari()).thenReturn(hikari);
        return config;
    }
}
