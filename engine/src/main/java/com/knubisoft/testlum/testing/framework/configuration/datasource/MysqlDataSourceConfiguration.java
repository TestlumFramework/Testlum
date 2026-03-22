package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
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
@Conditional({OnMysqlEnabledCondition.class})
@RequiredArgsConstructor
public class MysqlDataSourceConfiguration {

    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;
    private final DataSourceUtil dataSourceUtil;

    @Bean("mySqlDataSource")
    public Map<AliasEnv, DataSource> mysqlDataSource(final EnvToIntegrationMap envTointegrations) {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Mysql mysql : integrations.getMysqlIntegration().getMysql()) {
            if (mysql.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "MySQL", mysql.getAlias()),
                        () -> dataSourceUtil.getHikariDataSource(mysql),
                        forJdbc()
                );
                dataSourceMap.put(new AliasEnv(mysql.getAlias(), env), checkedDataSource);
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
