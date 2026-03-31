package com.knubisoft.testlum.testing.framework.configuration.datasource;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.framework.EnvToIntegrationMap;
import com.knubisoft.testlum.testing.framework.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.util.DataSourceUtil;
import com.knubisoft.testlum.testing.model.global_config.DatabaseConfig;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

@Configuration
@Conditional({OnPostgresEnabledCondition.class})
public class PostgresDataSourceConfiguration extends AbstractJdbcDataSourceConfiguration {

    public PostgresDataSourceConfiguration(final ConnectionTemplate connectionTemplate,
                                           final DataSourceUtil dataSourceUtil) {
        super(connectionTemplate, dataSourceUtil);
    }

    @Bean("postgresDataSource")
    public Map<AliasEnv, DataSource> postgresDataSource(final EnvToIntegrationMap envTointegrations) {
        return createDataSourceMap(envTointegrations);
    }

    @Override
    protected List<? extends DatabaseConfig> getIntegrationList(final Integrations integrations) {
        return integrations.getPostgresIntegration().getPostgres();
    }

    @Override
    protected String getIntegrationName() {
        return "Postgres";
    }
}
