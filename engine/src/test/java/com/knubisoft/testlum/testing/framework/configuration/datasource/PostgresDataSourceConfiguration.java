package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Conditional({OnPostgresEnabledCondition.class})
@RequiredArgsConstructor
public class PostgresDataSourceConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean("postgresDataSource")
    public Map<AliasEnv, DataSource> postgresDataSource() {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Postgres dataSource : integrations.getPostgresIntegration().getPostgres()) {
            if (dataSource.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        "Postgres - " + dataSource.getAlias(),
                        () -> {
                            DataSource hikariDataSource = DataSourceUtil.getHikariDataSource(dataSource);
                            try (Connection conn = hikariDataSource.getConnection();
                                 Statement stmt = conn.createStatement()) {
                                stmt.executeQuery("SELECT 1").next();
                                return hikariDataSource;

                            } catch (Exception e) {
                                if (hikariDataSource instanceof AutoCloseable closeable) {
                                    try {
                                        closeable.close();
                                    } catch (Exception ignored) {
                                        // ignored
                                    }
                                }
                                throw new DefaultFrameworkException(e.getMessage());
                            }
                        }
                );
                dataSourceMap.put(new AliasEnv(dataSource.getAlias(), env), checkedDataSource);
            }
        }
    }
}
