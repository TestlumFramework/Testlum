package com.knubisoft.e2e.testing.framework.configuration.datasource;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Postgres;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class PostgresDataSourceConfiguration {

    @Bean("postgresDataSource")
    @Conditional({OnPostgresEnabledCondition.class})
    public Map<String, DataSource> postgresDataSource() {
        Map<String, DataSource> postgreses = new HashMap<>();
        for (Postgres dataSource : GlobalTestConfigurationProvider.provide().getPostgreses().getPostgres()) {
            if (dataSource.isEnabled()) {
                postgreses.put(dataSource.getAlias(), getHikariDataSource(dataSource));
            }
        }
        return postgreses;
    }

    private DataSource getHikariDataSource(final Postgres dataSource) {
        HikariDataSource hikariDataSourceOriginal = new HikariDataSource();
        setDefaultHikariSettings(hikariDataSourceOriginal, dataSource);
        setAdditionalHikariSettings(hikariDataSourceOriginal, dataSource);
        return hikariDataSourceOriginal;
    }

    private void setAdditionalHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                             final Postgres dataSource) {
        hikariDataSourceOriginal.setMaximumPoolSize(dataSource.getHikari().getMaximumPoolSize());
        hikariDataSourceOriginal.setAutoCommit(dataSource.getHikari().isAutoCommit());
        hikariDataSourceOriginal.setConnectionInitSql(dataSource.getHikari().getConnectionInitSql());
        hikariDataSourceOriginal.setConnectionTestQuery(dataSource.getHikari().getConnectionTestQuery());
        hikariDataSourceOriginal.setPoolName(dataSource.getHikari().getPoolName());
        hikariDataSourceOriginal.setIdleTimeout(dataSource.getHikari().getIdleTimeout());
        hikariDataSourceOriginal.setMaxLifetime(dataSource.getHikari().getMaxLifetime());
    }

    private void setDefaultHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                          final Postgres dataSource) {
        hikariDataSourceOriginal.setDriverClassName(dataSource.getJdbcDriver());
        hikariDataSourceOriginal.setJdbcUrl(dataSource.getConnectionUrl());
        hikariDataSourceOriginal.setUsername(dataSource.getUsername());
        hikariDataSourceOriginal.setPassword(dataSource.getPassword());
    }

}
