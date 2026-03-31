package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

@Conditional({OnPostgresEnabledCondition.class})
@Component("postgresOperation")
public class PostgresSqlOperation extends AbstractSqlOperation {

    public PostgresSqlOperation(@Autowired(required = false) @Qualifier("postgresDataSource")
                                final Map<AliasEnv, DataSource> postgresDataSource) {
        super(postgresDataSource, Postgres.class);
    }

    @Override
    protected AbstractSqlExecutor createExecutor(final DataSource dataSource) {
        return new PostgresExecutor(dataSource);
    }
}
