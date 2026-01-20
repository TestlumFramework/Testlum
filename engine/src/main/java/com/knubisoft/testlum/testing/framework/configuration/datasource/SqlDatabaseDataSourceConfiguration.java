package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnSqlDatabaseEnableCondition.class})
public class SqlDatabaseDataSourceConfiguration {

    @Bean("sqlDatabaseDataSource")
    public Map<AliasEnv, DataSource> sqlDatabaseDataSource() {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        ConfigProviderImpl.GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (SqlDatabase dataSource : integrations.getSqlDatabaseIntegration().getSqlDatabase()) {
            if (dataSource.isEnabled()) {
                dataSourceMap.put(new AliasEnv(dataSource.getAlias(), env),
                        DataSourceUtil.getHikariDataSource(dataSource));
            }
        }
    }
}