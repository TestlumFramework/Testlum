package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.db.sql.executor.impl.ClickhouseExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManagerImpl.EnvProvider;
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
@Component("clickhouseOperation")
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
        List<QueryResult<Object>> clickhouseAppliedRecords =
                clickhouseExecutor.get(new AliasEnv(databaseAlias, EnvProvider.currentEnv()))
                        .executeQueries(queriesClickhouse);
        return new StorageOperationResult(clickhouseAppliedRecords);
    }

    @Override
    public void clearSystem() {
        clickhouseExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvProvider.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
