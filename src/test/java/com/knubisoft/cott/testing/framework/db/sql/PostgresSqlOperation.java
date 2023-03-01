package com.knubisoft.cott.testing.framework.db.sql;

import com.knubisoft.cott.runner.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.cott.testing.framework.db.sql.executor.impl.PostgresExecutor;
import com.knubisoft.cott.testing.model.AliasEnv;
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
@Component
public class PostgresSqlOperation implements StorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> postgresExecutor;

    public PostgresSqlOperation(@Autowired(required = false) @Qualifier("postgresDataSource")
                                final Map<AliasEnv, DataSource> postgresDataSource) {
        postgresExecutor = new HashMap<>();
        postgresDataSource.forEach((key, value) -> postgresExecutor.put(key, new PostgresExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesPostgres = source.getQueries();
        List<QueryResult<Object>> postgresAppliedRecords = postgresExecutor.get(AliasEnv.build(databaseAlias))
                .executeQueries(queriesPostgres);
        return new StorageOperationResult(postgresAppliedRecords);
    }

    @Override
    public void clearSystem() {
        postgresExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.getThreadEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
