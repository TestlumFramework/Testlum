package com.knubisoft.cott.testing.framework.configuration.datasource;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.util.DataSourceUtil;
import com.knubisoft.cott.testing.model.global_config.Integrations;
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
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((s, integrations) -> collectDataSource(oracleIntegration, s, integrations));
        return oracleIntegration;
    }

    private void collectDataSource(final Map<String, DataSource> oracleIntegration,
                                   final String envName, final Integrations integrations) {
        for (Oracle dataSource : integrations.getOracleIntegration().getOracle()) {
            if (dataSource.isEnabled()) {
                oracleIntegration.put(envName + DelimiterConstant.UNDERSCORE + dataSource.getAlias(),
                        DataSourceUtil.getHikariDataSource(dataSource));
            }
        }
    }
}
