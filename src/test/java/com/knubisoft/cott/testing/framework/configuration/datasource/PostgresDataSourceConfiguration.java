package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.cott.testing.framework.util.DataSourceUtil;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Postgres;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration
public class PostgresDataSourceConfiguration {

    @Bean("postgresDataSource")
    @Conditional({OnPostgresEnabledCondition.class})
    public Map<String, DataSource> postgresDataSource() {
        Map<String, DataSource> postgresIntegration = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((s, integrations) -> collectDataSource(postgresIntegration, s, integrations));
        return postgresIntegration;
    }

    private void collectDataSource(final Map<String, DataSource> postgresIntegration,
                                   final String envName, final Integrations integrations) {
        for (Postgres dataSource : integrations.getPostgresIntegration().getPostgres()) {
            if (dataSource.isEnabled()) {
                postgresIntegration.put(envName + UNDERSCORE + dataSource.getAlias(),
                        DataSourceUtil.getHikariDataSource(dataSource));
            }
        }
    }
}
