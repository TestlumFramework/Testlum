package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
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
@Conditional({OnMysqlEnabledCondition.class})
@RequiredArgsConstructor
public class MysqlDataSourceConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean("mySqlDataSource")
    public Map<AliasEnv, DataSource> mysqlDataSource() {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Mysql dataSource : integrations.getMysqlIntegration().getMysql()) {
            if (dataSource.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        "MySQL - " + dataSource.getAlias(),
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
