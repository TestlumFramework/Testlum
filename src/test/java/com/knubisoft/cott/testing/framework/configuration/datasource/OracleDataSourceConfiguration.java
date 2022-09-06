package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.cott.testing.framework.util.DataSourceUtil;
import com.knubisoft.cott.testing.model.global_config.Oracle;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class OracleDataSourceConfiguration {

    @Bean("oracleDataSource")
    @Conditional({OnOracleEnabledCondition.class})
    public Map<String, DataSource> oracleDataSource() {
        Map<String, DataSource> oracleIntegration = new HashMap<>();
        for (Oracle dataSource : GlobalTestConfigurationProvider.getIntegrations().getOracleIntegration().getOracle()) {
            if (dataSource.isEnabled()) {
                oracleIntegration.put(dataSource.getAlias(), DataSourceUtil.getHikariDataSource(dataSource));
            }
        }
        return oracleIntegration;
    }
}
