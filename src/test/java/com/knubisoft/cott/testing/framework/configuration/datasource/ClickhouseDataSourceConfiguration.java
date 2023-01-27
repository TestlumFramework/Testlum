package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.Clickhouse;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseProperties;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration("clickhouseDataSourceConfiguration")
@Conditional({OnClickhouseEnabledCondition.class})
public class ClickhouseDataSourceConfiguration {

    @Bean("clickhouseDataSource")
    public Map<String, DataSource> dataSource() {
        final Map<String, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((s, integrations) -> collectDataSource(dataSourceMap, s, integrations));
        return dataSourceMap;
    }

    private void collectDataSource(final Map<String, DataSource> clickhouseIntegration,
                                   final String envName, final Integrations integrations) {
        for (Clickhouse clickhouse
                : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                ClickHouseProperties properties = clickHouseProperties(clickhouse);
                String url = clickhouse.getConnectionUrl();
                clickhouseIntegration.put(envName + UNDERSCORE + clickhouse.getAlias(),
                        new ClickHouseDataSource(url, properties));
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
