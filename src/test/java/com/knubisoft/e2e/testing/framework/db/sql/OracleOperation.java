package com.knubisoft.e2e.testing.framework.db.sql;

import com.knubisoft.e2e.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.e2e.testing.framework.db.sql.executor.impl.OracleExecutor;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.Source;
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
@Conditional({OnOracleEnabledCondition.class})
@Component
public class OracleOperation implements StorageOperation {

    private final Map<String, AbstractSqlExecutor> oracleExecutor;

    public OracleOperation(@Autowired(required = false) @Qualifier("oracleDataSource")
                           final Map<String, DataSource> oracleDataSource) {
        oracleExecutor = new HashMap<>();
        oracleDataSource.forEach((key, value) -> oracleExecutor.put(key, new OracleExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseName) {
        List<String> queriesOracle = source.getQueries();
        List<QueryResult<Object>> oracleAppliedRecords = oracleExecutor.get(databaseName).executeQueries(queriesOracle);
        return new StorageOperationResult(oracleAppliedRecords);
    }

    @Override
    public void clearSystem() {
        oracleExecutor.forEach((key, value) -> value.truncate());
    }
}

