package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.configuration.connection.health.HealthCheckFactory;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_INTEGRATION_DATA;

@Configuration
@Conditional({OnMysqlEnabledCondition.class})
@RequiredArgsConstructor
public class MysqlDataSourceConfiguration {

    @Autowired(required = false)
    private final ConnectionTemplate connectionTemplate;

    @Bean("mySqlDataSource")
    public Map<AliasEnv, DataSource> mysqlDataSource() {
        Map<AliasEnv, DataSource> dataSourceMap = new HashMap<>();
        GlobalTestConfigurationProvider.get().getIntegrations()
                .forEach((env, integrations) -> collectDataSource(integrations, env, dataSourceMap));
        return dataSourceMap;
    }

    private void collectDataSource(final Integrations integrations,
                                   final String env,
                                   final Map<AliasEnv, DataSource> dataSourceMap) {
        for (Mysql mysql : integrations.getMysqlIntegration().getMysql()) {
            if (mysql.isEnabled()) {
                DataSource checkedDataSource = connectionTemplate.executeWithRetry(
                        String.format(CONNECTION_INTEGRATION_DATA, "MySQL", mysql.getAlias()),
                        () -> DataSourceUtil.getHikariDataSource(mysql),
                        HealthCheckFactory.forJdbc()
                );
                dataSourceMap.put(new AliasEnv(mysql.getAlias(), env), checkedDataSource);
            }
        }
    }
}
