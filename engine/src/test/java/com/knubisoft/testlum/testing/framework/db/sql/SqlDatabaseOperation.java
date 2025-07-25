package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.db.sql.executor.impl.SqlDatabaseExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
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
@Conditional({OnSqlDatabaseEnableCondition.class})
@Component("sqlDatabaseOperation")
public class SqlDatabaseOperation extends AbstractStorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> sqlExecutors;

    public SqlDatabaseOperation(@Autowired(required = false) @Qualifier("sqlDatabaseDataSource")
                                final Map<AliasEnv, DataSource> sqlDatabaseDataSource) {
        sqlExecutors = new HashMap<>();
        sqlDatabaseDataSource.forEach((key, value) ->
                sqlExecutors.put(key, new SqlDatabaseExecutor(value, key))
        );
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesSqlDatabase = source.getQueries();
        List<QueryResult<Object>> sqlDatabaseAppliedRecords =
                sqlExecutors.get(new AliasEnv(databaseAlias, EnvManager.currentEnv()))
                        .executeQueries(queriesSqlDatabase);
        return new StorageOperationResult(sqlDatabaseAppliedRecords);
    }

    @Override
    public void clearSystem() {
        sqlExecutors.forEach((aliasEnv, sqlExecutor) -> {
            if (isTruncate(SqlDatabase.class, aliasEnv)
                && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}