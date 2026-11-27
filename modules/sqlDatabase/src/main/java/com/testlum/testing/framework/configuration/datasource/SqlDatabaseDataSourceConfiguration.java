package com.testlum.testing.framework.configuration.datasource;

import com.testlum.testing.connection.ConnectionTemplate;
import com.testlum.testing.framework.EnvToIntegrationMap;
import com.testlum.testing.framework.condition.OnSqlDatabaseEnableCondition;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.util.DataSourceUtil;
import com.testlum.testing.model.global_config.DatabaseConfig;
import com.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@Conditional({OnSqlDatabaseEnableCondition.class})
public class SqlDatabaseDataSourceConfiguration extends AbstractJdbcDataSourceConfiguration {

    public SqlDatabaseDataSourceConfiguration(final ConnectionTemplate connectionTemplate,
                                              final DataSourceUtil dataSourceUtil) {
        super(connectionTemplate, dataSourceUtil);
    }

    @Bean("sqlDatabaseDataSource")
    public Map<AliasEnv, DataSource> sqlDatabaseDataSource(final EnvToIntegrationMap envTointegrations) {
        return createDataSourceMap(envTointegrations);
    }

    @Override
    protected List<? extends DatabaseConfig> getIntegrationList(final Integrations integrations) {
        return integrations.getSqlDatabaseIntegration().getSqlDatabase();
    }

    @Override
    protected String getIntegrationName() {
        return "SqlDatabase";
    }
}
