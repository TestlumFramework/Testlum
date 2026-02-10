package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnSqlDatabaseEnableCondition.class})
@RequiredArgsConstructor
public class SqlDatabaseDataSourceConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean("sqlDatabaseDataSource")
    public Map<AliasEnv, DataSource> sqlDatabaseDataSource() {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (SqlDatabase sqlDatabase : integrations.getSqlDatabaseIntegration().getSqlDatabase()) {
            if (sqlDatabase.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "SqlDatabase", sqlDatabase.getAlias()),
                        () -> DataSourceUtil.getHikariDataSource(sqlDatabase),
                        HealthCheckFactory.forJdbc()
                );
                dataSourceMap.put(new AliasEnv(sqlDatabase.getAlias(), env), checkedDataSource);
            }
        }
    }
}