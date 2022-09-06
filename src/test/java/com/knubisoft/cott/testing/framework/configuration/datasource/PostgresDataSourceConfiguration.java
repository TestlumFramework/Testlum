package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.cott.testing.framework.util.DataSourceUtil;
import com.knubisoft.cott.testing.model.global_config.Postgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class PostgresDataSourceConfiguration {

    @Bean("postgresDataSource")
    @Conditional({OnPostgresEnabledCondition.class})
    public Map<String, DataSource> postgresDataSource() {
        Map<String, DataSource> postgresIntegration = new HashMap<>();
        for (Postgres dataSource
                : GlobalTestConfigurationProvider.getIntegrations().getPostgresIntegration().getPostgres()) {
            if (dataSource.isEnabled()) {
                postgresIntegration.put(dataSource.getAlias(), DataSourceUtil.getHikariDataSource(dataSource));
            }
        }
        return postgresIntegration;
    }
}
