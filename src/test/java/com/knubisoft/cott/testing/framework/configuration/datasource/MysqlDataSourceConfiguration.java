package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.cott.testing.framework.util.DataSourceUtil;
import com.knubisoft.cott.testing.model.global_config.Integrations;
import com.knubisoft.cott.testing.model.global_config.Mysql;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;

@Configuration
@Conditional({OnMysqlEnabledCondition.class})
public class MysqlDataSourceConfiguration {

    @Bean("mySqlDataSource")
    public Map<String, DataSource> mysqlDataSource() {
        Map<String, DataSource> mysqlIntegration = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((s, integrations) -> collectDataSource(mysqlIntegration, s, integrations));
        return mysqlIntegration;
    }

    private void collectDataSource(final Map<String, DataSource> mysqlIntegration,
                                   final String envName, final Integrations integrations) {
        for (Mysql dataSource : integrations.getMysqlIntegration().getMysql()) {
            if (dataSource.isEnabled()) {
                mysqlIntegration.put(envName + UNDERSCORE + dataSource.getAlias(),
                        DataSourceUtil.getHikariDataSource(dataSource));
            }
        }
    }
}
