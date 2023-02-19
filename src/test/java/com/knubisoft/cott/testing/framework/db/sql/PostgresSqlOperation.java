package com.knubisoft.cott.testing.framework.db.sql;

import com.knubisoft.cott.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.cott.testing.framework.db.sql.executor.impl.PostgresExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Conditional({OnPostgresEnabledCondition.class})
@Component
public class PostgresSqlOperation implements StorageOperation {
    private final Map<String, AbstractSqlExecutor> postgresExecutor;

    public PostgresSqlOperation(@Autowired(required = false) @Qualifier("postgresDataSource")
                                final Map<String, DataSource> postgresDataSource) {
        postgresExecutor = new HashMap<>();
        postgresDataSource.forEach((key, value) -> postgresExecutor.put(key, new PostgresExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseName) {
        List<String> queriesPostgres = source.getQueries();
        List<QueryResult<Object>> postgresAppliedRecords = postgresExecutor.get(databaseName)
                .executeQueries(queriesPostgres);
        return new StorageOperationResult(postgresAppliedRecords);
    }

    @Override
    public void clearSystem() {
        postgresExecutor.forEach((key, value) -> value.truncate());
    }
}
