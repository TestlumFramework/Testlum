package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.clickhouse.jdbc.DataSourceImpl;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnClickhouseEnabledCondition.class})
@RequiredArgsConstructor
public class ClickhouseDataSourceConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean("clickhouseDataSource")
    public Map<AliasEnv, DataSource> dataSource() {
        final Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "ClickHouse", clickhouse.getAlias()),
                        () -> new DataSourceImpl(clickhouse.getConnectionUrl(), clickHouseProperties(clickhouse)),
                        HealthCheckFactory.forJdbc()
                );
                dataSourceMap.put(new AliasEnv(clickhouse.getAlias(), env), checkedDataSource);
            }
        }
        addIfEnabled(integrations, env, dataSourceMap);
    }

    private void addIfEnabled(final Integrations integrations,
                              final String env,
                              final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                dataSourceMap.put(new AliasEnv(clickhouse.getAlias(), env),
                        new DataSourceImpl(clickhouse.getConnectionUrl(), clickHouseProperties(clickhouse)));
            }
        }
    }

    private Properties clickHouseProperties(final Clickhouse clickhouse) {
        Properties properties = new Properties();
        properties.put("user", clickhouse.getUsername());
        properties.put("password", clickhouse.getPassword());
        return properties;
    }
}