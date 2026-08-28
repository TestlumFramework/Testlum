package com.testlum.testing.framework.db.sql;

import com.testlum.testing.framework.FileSearcher;
import com.testlum.testing.framework.condition.OnSqlDatabaseEnableCondition;
import com.testlum.testing.framework.db.AbstractStorageOperation;
import com.testlum.testing.framework.db.source.Source;
import com.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.testlum.testing.framework.env.AliasEnv;
import com.testlum.testing.framework.env.EnvManager;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.global_config.SqlDatabase;
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
                                final Map<AliasEnv, DataSource> sqlDatabaseDataSource,
                                final FileSearcher fileSearcher,
                                final Integrations integrations) {
        sqlExecutors = new HashMap<>();
        sqlDatabaseDataSource.forEach((key, value) ->
                sqlExecutors.put(key, new SqlDatabaseExecutor(fileSearcher, value, key, integrations))
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