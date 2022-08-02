package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Oracle;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OracleDataSourceConfiguration {

    @Bean("oracleDatasource")
    @Conditional({OnOracleEnabledCondition.class})
    public Map<String, DataSource> postgresDataSource() {
        Map<String, DataSource> oracleIntegration = new HashMap<>();
        for (Oracle dataSource : GlobalTestConfigurationProvider.getIntegrations().getOracleIntegration().getOracle()) {
            if (dataSource.isEnabled()) {
                oracleIntegration.put(dataSource.getAlias(), getHikariDataSource(dataSource));
            }
        }
        return oracleIntegration;
    }

    private DataSource getHikariDataSource(final Oracle dataSource) {
        HikariDataSource hikariDataSourceOriginal = new HikariDataSource();
        setDefaultHikariSettings(hikariDataSourceOriginal, dataSource);
        setAdditionalHikariSettings(hikariDataSourceOriginal, dataSource);
        return hikariDataSourceOriginal;
    }

    private void setAdditionalHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                             final Oracle dataSource) {
        hikariDataSourceOriginal.setMaximumPoolSize(dataSource.getHikari().getMaximumPoolSize());
        hikariDataSourceOriginal.setAutoCommit(dataSource.getHikari().isAutoCommit());
        hikariDataSourceOriginal.setConnectionInitSql(dataSource.getHikari().getConnectionInitSql());
        hikariDataSourceOriginal.setConnectionTestQuery(dataSource.getHikari().getConnectionTestQuery());
        hikariDataSourceOriginal.setPoolName(dataSource.getHikari().getPoolName());
        hikariDataSourceOriginal.setIdleTimeout(dataSource.getHikari().getIdleTimeout());
        hikariDataSourceOriginal.setMaxLifetime(dataSource.getHikari().getMaxLifetime());
    }

    private void setDefaultHikariSettings(final HikariDataSource hikariDataSourceOriginal,
                                          final Oracle dataSource) {
        hikariDataSourceOriginal.setDriverClassName(dataSource.getJdbcDriver());
        hikariDataSourceOriginal.setJdbcUrl(dataSource.getConnectionUrl());
        hikariDataSourceOriginal.setUsername(dataSource.getUsername());
        hikariDataSourceOriginal.setPassword(dataSource.getPassword());
    }

}
