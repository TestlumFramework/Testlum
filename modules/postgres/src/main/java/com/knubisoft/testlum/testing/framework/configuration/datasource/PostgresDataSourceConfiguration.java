package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnPostgresEnabledCondition.class})
@RequiredArgsConstructor
public class PostgresDataSourceConfiguration {

    private static final int TIME = 5;

    private final ConnectionTemplate connectionTemplate;
    private final DataSourceUtil dataSourceUtil;

    @Bean("postgresDataSource")
    public Map<AliasEnv, DataSource> postgresDataSource(final EnvToIntegrationMap envTointegrations) {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Postgres postgres : integrations.getPostgresIntegration().getPostgres()) {
            if (postgres.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Postgres", postgres.getAlias()),
                        () -> dataSourceUtil.getHikariDataSource(postgres),
                        forJdbc()
                );
                dataSourceMap.put(new AliasEnv(postgres.getAlias(), env), checkedDataSource);
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
