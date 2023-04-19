package com.knubisoft.testlum.testing.framework.util;


import com.knubisoft.testlum.testing.model.global_config.DatabaseConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.experimental.UtilityClass;
import org.apache.commons.lang3.StringUtils;

import javax.sql.DataSource;

@UtilityClass
public final class DataSourceUtil {

    public DataSource getHikariDataSource(final DatabaseConfig dataSource) {
        HikariDataSource hikariDataSourceOriginal = new HikariDataSource();
        setDefaultHikariSettings(hikariDataSourceOriginal, dataSource);
        setAdditionalHikariSettings(hikariDataSourceOriginal, dataSource);
        if (StringUtils.isNotBlank(dataSource.getSchema())) {
            hikariDataSourceOriginal.setSchema(dataSource.getSchema());
        }
        return hikariDataSourceOriginal;
    }

    private void setDefaultHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                          final DatabaseConfig dataSource) {
        hikariDataSourceOriginal.setDriverClassName(dataSource.getJdbcDriver());
        hikariDataSourceOriginal.setJdbcUrl(dataSource.getConnectionUrl());
        hikariDataSourceOriginal.setUsername(dataSource.getUsername());
        hikariDataSourceOriginal.setPassword(dataSource.getPassword());
    }

    private void setAdditionalHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                             final DatabaseConfig dataSource) {
        hikariDataSourceOriginal.setMaximumPoolSize(dataSource.getHikari().getMaximumPoolSize());
        hikariDataSourceOriginal.setAutoCommit(dataSource.getHikari().isAutoCommit());
        hikariDataSourceOriginal.setConnectionInitSql(dataSource.getHikari().getConnectionInitSql());
        hikariDataSourceOriginal.setConnectionTestQuery(dataSource.getHikari().getConnectionTestQuery());
        hikariDataSourceOriginal.setPoolName(dataSource.getHikari().getPoolName());
        hikariDataSourceOriginal.setIdleTimeout(dataSource.getHikari().getIdleTimeout());
        hikariDataSourceOriginal.setMinimumIdle(dataSource.getHikari().getMinimumIdle());
        hikariDataSourceOriginal.setMaxLifetime(dataSource.getHikari().getMaxLifetime());
    }
}
