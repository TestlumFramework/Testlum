package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.clickhouse.jdbc.DataSourceImpl;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@Conditional({OnClickhouseEnabledCondition.class})
@RequiredArgsConstructor
public class ClickhouseDataSourceConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean("clickhouseDataSource")
    public Map<AliasEnv, DataSource> dataSource() {
        final Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {

        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {

                DataSource authenticatedDataSource = connectionTemplate.executeWithRetry(
                        "ClickHouse - " + clickhouse.getAlias(),
                        () -> {
                            Properties properties = clickHouseProperties(clickhouse);
                            String url = clickhouse.getConnectionUrl();
                            DataSource ds = new DataSourceImpl(url, properties);

                            try (Connection conn = ds.getConnection();
                                 Statement stmt = conn.createStatement()) {
                                stmt.executeQuery("SELECT 1").next();
                                return ds;
                            } catch (Exception e) {
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        }
                );

                dataSourceMap.put(new AliasEnv(clickhouse.getAlias(), env), authenticatedDataSource);
            }
        }

        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                Properties properties = clickHouseProperties(clickhouse);
                String url = clickhouse.getConnectionUrl();
                dataSourceMap.put(new AliasEnv(clickhouse.getAlias(), env), new DataSourceImpl(url, properties));
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