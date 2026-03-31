package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.DatabaseConfig;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public abstract class AbstractJdbcDataSourceConfiguration {

    private static final int TIME = 5;

    protected final ConnectionTemplate connectionTemplate;
    protected final DataSourceUtil dataSourceUtil;

    protected Map<AliasEnv, DataSource> createDataSourceMap(final EnvToIntegrationMap envTointegrations) {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        envTointegrations.forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (DatabaseConfig dbConfig : getIntegrationList(integrations)) {
            if (dbConfig.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(LogMessage.CONNECTION_INTEGRATION_DATA,
                                getIntegrationName(), dbConfig.getAlias()),
                        () -> dataSourceUtil.getHikariDataSource(dbConfig),
                        forJdbc()
                );
                dataSourceMap.put(new AliasEnv(dbConfig.getAlias(), env), checkedDataSource);
            }
        }
    }

    protected abstract List<? extends DatabaseConfig> getIntegrationList(Integrations integrations);

    protected abstract String getIntegrationName();

    private IntegrationHealthCheck<DataSource> forJdbc() {
        return ds -> {
            try (Connection conn = ds.getConnection()) {
                conn.isValid(TIME);
            }
        };
    }
}
