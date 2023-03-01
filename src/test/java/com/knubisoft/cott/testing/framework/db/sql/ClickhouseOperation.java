package com.knubisoft.cott.testing.framework.db.sql;

import com.knubisoft.cott.runner.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.cott.testing.framework.db.sql.executor.impl.ClickhouseExecutor;
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
@Conditional({OnClickhouseEnabledCondition.class})
@Component
public class ClickhouseOperation implements StorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> clickhouseExecutor;

    public ClickhouseOperation(@Autowired @Qualifier("clickhouseDataSource")
                               final Map<AliasEnv, DataSource> clickhouseDataSource) {
        clickhouseExecutor = new HashMap<>();
        clickhouseDataSource.forEach((key, value) -> clickhouseExecutor.put(key, new ClickhouseExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesClickhouse = source.getQueries();
        List<QueryResult<Object>> clickhouseAppliedRecords = clickhouseExecutor.get(AliasEnv.build(databaseAlias))
                .executeQueries(queriesClickhouse);
        return new StorageOperationResult(clickhouseAppliedRecords);
    }

    @Override
    public void clearSystem() {
        clickhouseExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.getThreadEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
