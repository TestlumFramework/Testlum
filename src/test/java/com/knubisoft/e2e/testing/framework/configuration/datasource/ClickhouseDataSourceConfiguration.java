package com.knubisoft.e2e.testing.framework.configuration.datasource;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.e2e.testing.model.global_config.Clickhouse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration("clickhouseDataSourceConfiguration")
@Conditional({OnClickhouseEnabledCondition.class})
public class ClickhouseDataSourceConfiguration {

    @Bean("clickhouseDataSource")
    public Map<String, DataSource> dataSource() {
        final Map<String, DataSource> dataSourceMap = new HashMap<>();
        for (Clickhouse clickhouse
                : GlobalTestConfigurationProvider.getIntegrations().getClickhouses().getClickhouse()) {
            ClickHouseProperties properties = clickHouseProperties(clickhouse);
            String url = clickhouse.getConnectionUrl();
            dataSourceMap.put(clickhouse.getAlias(), new ClickHouseDataSource(url, properties));
        }
        return dataSourceMap;
    }

    private ClickHouseProperties clickHouseProperties(final Clickhouse clickhouse) {
        ClickHouseProperties properties = new ClickHouseProperties();
        properties.setUser(clickhouse.getUsername());
        properties.setPassword(clickhouse.getPassword());
        return properties;
    }

}
