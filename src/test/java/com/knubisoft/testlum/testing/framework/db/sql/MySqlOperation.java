package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.db.sql.executor.impl.MySqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
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
@Component("mySqlOperation")
public class MySqlOperation implements StorageOperation {

    @Autowired
    private GlobalTestConfigurationProvider configurationProvider;
    @Autowired
    private EnvManager envManager;
    private final Map<AliasEnv, AbstractSqlExecutor> mySqlExecutor;

    public MySqlOperation(@Autowired(required = false) @Qualifier("mySqlDataSource")
                          final Map<AliasEnv, DataSource> mySqlDataSource) {
        mySqlExecutor = new HashMap<>();
        mySqlDataSource.forEach((key, value) -> mySqlExecutor.put(key, new MySqlExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesMySql = source.getQueries();
        List<QueryResult<Object>> mySqlAppliedRecords =
                mySqlExecutor.get(new AliasEnv(databaseAlias, envManager.currentEnv())).executeQueries(queriesMySql);
        return new StorageOperationResult(mySqlAppliedRecords);
    }

    @Override
    public void clearSystem() {
        mySqlExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (isTruncate(Mysql.class, aliasEnv, configurationProvider)
                    && Objects.equals(aliasEnv.getEnvironment(), envManager.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
