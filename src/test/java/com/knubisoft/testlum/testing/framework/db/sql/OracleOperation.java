package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.testlum.testing.framework.db.StorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.db.sql.executor.impl.OracleExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
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
@Conditional({OnOracleEnabledCondition.class})
@Component
public class OracleOperation implements StorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> oracleExecutor;

    public OracleOperation(@Autowired(required = false) @Qualifier("oracleDataSource")
                           final Map<AliasEnv, DataSource> oracleDataSource) {
        oracleExecutor = new HashMap<>();
        oracleDataSource.forEach((key, value) -> oracleExecutor.put(key, new OracleExecutor(value)));
    }

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queriesOracle = source.getQueries();
        List<QueryResult<Object>> oracleAppliedRecords = oracleExecutor.get(AliasEnv.build(databaseAlias))
                .executeQueries(queriesOracle);
        return new StorageOperationResult(oracleAppliedRecords);
    }

    @Override
    public void clearSystem() {
        oracleExecutor.forEach((aliasEnv, sqlExecutor) -> {
            if (isTruncate(Oracle.class, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
