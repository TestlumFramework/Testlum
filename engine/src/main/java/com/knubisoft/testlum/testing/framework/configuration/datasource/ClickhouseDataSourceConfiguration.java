package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.clickhouse.jdbc.DataSourceImpl;
import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.GlobalTestConfigurationProvider.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;

@Configuration
@Conditional({OnClickhouseEnabledCondition.class})
@RequiredArgsConstructor
public class ClickhouseDataSourceConfiguration {

    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;

    @Bean("clickhouseDataSource")
    public Map<AliasEnv, DataSource> dataSource(final EnvToIntegrationMap envTointegrations) {
        final Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) ->
                        collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "ClickHouse", clickhouse.getAlias()),
                        () -> new DataSourceImpl(clickhouse.getConnectionUrl(), clickHouseProperties(clickhouse)),
                        forJdbc()
                );
                dataSourceMap.put(new AliasEnv(clickhouse.getAlias(), env), checkedDataSource);
            }
        }
    }

    private Properties clickHouseProperties(final Clickhouse clickhouse) {
        Properties properties = new Properties();
        properties.put("user", clickhouse.getUsername());
        properties.put("password", clickhouse.getPassword());
        return properties;
    }

    private IntegrationHealthCheck<DataSource> forJdbc() {
        return ds -> {
            try (Connection conn = ds.getConnection()) {
                conn.isValid(TIME);
            }
        };
    }
}