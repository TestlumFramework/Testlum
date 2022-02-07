package com.knubisoft.e2e.testing.framework.db.sql;

import com.knubisoft.e2e.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.e2e.testing.framework.db.StorageOperation;
import com.knubisoft.e2e.testing.framework.db.source.Source;
import com.knubisoft.e2e.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.e2e.testing.framework.db.sql.executor.impl.ClickhouseExecutor;
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
@Conditional({OnClickhouseEnabledCondition.class})
@Component
public class ClickhouseOperation implements StorageOperation {
    private final Map<String, AbstractSqlExecutor> clickhouseExecutor;

    public ClickhouseOperation(@Autowired @Qualifier("clickhouseDataSource")
                               final Map<String, DataSource> clickhouseDataSource) {
        clickhouseExecutor = new HashMap<>();
        clickhouseDataSource.forEach((key, value) -> clickhouseExecutor.put(key, new ClickhouseExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseName) {
        final List<String> queriesClickhouse = source.getQueries();
        final List<QueryResult<Object>> clickhouseAppliedRecords = clickhouseExecutor.get(databaseName)
                .executeQueries(queriesClickhouse);
        return new StorageOperationResult(clickhouseAppliedRecords);
    }

    @Override
    public void clearSystem() {
        clickhouseExecutor.forEach((key, value) -> value.truncate());
    }
}
