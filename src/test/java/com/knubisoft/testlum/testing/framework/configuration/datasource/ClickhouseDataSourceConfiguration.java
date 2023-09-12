package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnClickhouseEnabledCondition.class})
public class ClickhouseDataSourceConfiguration {

    @Bean("clickhouseDataSource")
    public Map<AliasEnv, DataSource> dataSource() {
        final Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        ConfigProvider.getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                ClickHouseProperties properties = clickHouseProperties(clickhouse);
                String url = clickhouse.getConnectionUrl();
                dataSourceMap.put(new AliasEnv(clickhouse.getAlias(), env), new ClickHouseDataSource(url, properties));
            }
        }
    }

    private ClickHouseProperties clickHouseProperties(final Clickhouse clickhouse) {
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUser(clickhouse.getUsername());
        properties.setPassword(clickhouse.getPassword());
        return properties;
    }
}
