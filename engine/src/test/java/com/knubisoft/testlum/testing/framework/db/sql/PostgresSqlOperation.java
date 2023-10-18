package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.db.sql.executor.impl.PostgresExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Conditional({OnPostgresEnabledCondition.class})
@Component("postgresOperation")
public class PostgresSqlOperation extends AbstractStorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> postgresExecutor;

    public PostgresSqlOperation(@Autowired(required = false) @Qualifier("postgresDataSource")
                                final Map<AliasEnv, DataSource> postgresDataSource) {
        postgresExecutor = new HashMap<>();
        postgresDataSource.forEach((key, value) -> postgresExecutor.put(key, new PostgresExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesPostgres = source.getQueries();
        List<QueryResult<Object>> postgresAppliedRecords =
                postgresExecutor.get(new AliasEnv(databaseAlias, EnvManager.currentEnv()))
                .executeQueries(queriesPostgres);
        return new StorageOperationResult(postgresAppliedRecords);
    }

    @Override
    public void clearSystem() {
        postgresExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (isTruncate(Postgres.class, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
