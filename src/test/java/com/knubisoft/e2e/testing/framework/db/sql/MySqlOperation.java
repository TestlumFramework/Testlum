package com.knubisoft.e2e.testing.framework.db.sql;

import com.knubisoft.e2e.testing.framework.db.sql.executor.impl.MySqlExecutor;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnMysqlEnabledCondition;
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
@Conditional({OnMysqlEnabledCondition.class})
@Component
public class MySqlOperation implements StorageOperation {

    private final Map<String, MySqlExecutor> mySqlExecutor;

    public MySqlOperation(@Autowired(required = false) @Qualifier("mySqlDataSource")
                          final Map<String, DataSource> mySqlDataSource) {
        mySqlExecutor = new HashMap<>();
        mySqlDataSource.forEach((key, value) -> mySqlExecutor.put(key, new MySqlExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseName) {
        List<String> queriesMySql = source.getQueries();
        List<QueryResult<Object>> mySqlAppliedRecords = mySqlExecutor.get(databaseName).executeQueries(queriesMySql);
        return new StorageOperationResult(mySqlAppliedRecords);
    }

    @Override
    public void clearSystem() {
        mySqlExecutor.forEach((key, value) -> value.truncate(key));
    }
}
