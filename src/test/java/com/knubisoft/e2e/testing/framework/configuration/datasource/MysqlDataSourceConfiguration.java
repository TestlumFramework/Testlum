package com.knubisoft.e2e.testing.framework.configuration.datasource;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Mysql;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnMysqlEnabledCondition.class})
public class MysqlDataSourceConfiguration {

    @Bean("mySqlDataSource")
    public Map<String, DataSource> mysqlDataSource() {
        Map<String, DataSource> mysqls = new HashMap<>();
        for (Mysql dataSource : GlobalTestConfigurationProvider.getIntegrations().getMysqls().getMysql()) {
            if (dataSource.isEnabled()) {
                mysqls.put(dataSource.getAlias(), getHikariDataSource(dataSource));
            }
        }
        return mysqls;
    }

    private DataSource getHikariDataSource(final Mysql dataSource) {
        HikariDataSource hikariDataSourceOriginal = new HikariDataSource();
        setDefaultHikariSettings(hikariDataSourceOriginal, dataSource);
        setAdditionalHikariSettings(hikariDataSourceOriginal, dataSource);
        return hikariDataSourceOriginal;
    }

    private void setAdditionalHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                             final Mysql dataSource) {
        hikariDataSourceOriginal.setMaximumPoolSize(dataSource.getHikari().getMaximumPoolSize());
        hikariDataSourceOriginal.setAutoCommit(dataSource.getHikari().isAutoCommit());
        hikariDataSourceOriginal.setConnectionInitSql(dataSource.getHikari().getConnectionInitSql());
        hikariDataSourceOriginal.setConnectionTestQuery(dataSource.getHikari().getConnectionTestQuery());
        hikariDataSourceOriginal.setPoolName(dataSource.getHikari().getPoolName());
        hikariDataSourceOriginal.setIdleTimeout(dataSource.getHikari().getIdleTimeout());
        hikariDataSourceOriginal.setMaxLifetime(dataSource.getHikari().getMaxLifetime());
    }

    private void setDefaultHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                          final Mysql dataSource) {
        hikariDataSourceOriginal.setDriverClassName(dataSource.getJdbcDriver());
        hikariDataSourceOriginal.setJdbcUrl(dataSource.getConnectionUrl());
        hikariDataSourceOriginal.setUsername(dataSource.getUsername());
        hikariDataSourceOriginal.setPassword(dataSource.getPassword());
    }

}
