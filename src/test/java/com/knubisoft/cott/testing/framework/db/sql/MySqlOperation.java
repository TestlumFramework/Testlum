package com.knubisoft.cott.testing.framework.db.sql;

import com.knubisoft.cott.testing.framework.env.EnvManager;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.cott.testing.framework.db.StorageOperation;
import com.knubisoft.cott.testing.framework.db.source.Source;
import com.knubisoft.cott.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.cott.testing.framework.db.sql.executor.impl.MySqlExecutor;
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
@Conditional({OnMysqlEnabledCondition.class})
@Component
public class MySqlOperation implements StorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> mySqlExecutor;

    public MySqlOperation(@Autowired(required = false) @Qualifier("mySqlDataSource")
                          final Map<AliasEnv, DataSource> mySqlDataSource) {
        mySqlExecutor = new HashMap<>();
        mySqlDataSource.forEach((key, value) -> mySqlExecutor.put(key, new MySqlExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesMySql = source.getQueries();
        List<QueryResult<Object>> mySqlAppliedRecords = mySqlExecutor.get(AliasEnv.build(databaseAlias))
                .executeQueries(queriesMySql);
        return new StorageOperationResult(mySqlAppliedRecords);
    }

    @Override
    public void clearSystem() {
        mySqlExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
