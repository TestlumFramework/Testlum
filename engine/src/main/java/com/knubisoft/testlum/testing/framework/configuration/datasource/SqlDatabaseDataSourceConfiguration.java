package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import com.knubisoft.testlum.testing.framework.GlobalTestConfigurationProvider.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;

@Configuration
@Conditional({OnSqlDatabaseEnableCondition.class})
@RequiredArgsConstructor
public class SqlDatabaseDataSourceConfiguration {

    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;
    private final DataSourceUtil dataSourceUtil;

    @Bean("sqlDatabaseDataSource")
    public Map<AliasEnv, DataSource> sqlDatabaseDataSource(final EnvToIntegrationMap envTointegrations) {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (SqlDatabase sqlDatabase : integrations.getSqlDatabaseIntegration().getSqlDatabase()) {
            if (sqlDatabase.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "SqlDatabase", sqlDatabase.getAlias()),
                        () -> dataSourceUtil.getHikariDataSource(sqlDatabase),
                        forJdbc()
                );
                dataSourceMap.put(new AliasEnv(sqlDatabase.getAlias(), env), checkedDataSource);
            }
        }
    }

    private IntegrationHealthCheck<DataSource> forJdbc() {
        return ds -> {
            try (Connection conn = ds.getConnection()) {
                conn.isValid(TIME);
            }
        };
    }
}