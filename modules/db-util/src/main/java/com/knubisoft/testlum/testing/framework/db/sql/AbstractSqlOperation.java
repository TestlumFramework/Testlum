package com.knubisoft.testlum.testing.framework.db.sql;

import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.source.Source;
import com.knubisoft.testlum.testing.framework.db.sql.executor.AbstractSqlExecutor;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.model.global_config.StorageIntegration;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class AbstractSqlOperation extends AbstractStorageOperation {

    private final Map<AliasEnv, AbstractSqlExecutor> executors;
    private final Class<? extends StorageIntegration> integrationType;

    protected AbstractSqlOperation(final Map<AliasEnv, DataSource> dataSources,
                                   final Class<? extends StorageIntegration> integrationType) {
        this.integrationType = integrationType;
        this.executors = new HashMap<>();
        dataSources.forEach((key, value) -> executors.put(key, createExecutor(value)));
    }

    protected abstract AbstractSqlExecutor createExecutor(DataSource dataSource);

    @Override
    public StorageOperationResult apply(final Source source, final String databaseAlias) {
        List<String> queries = source.getQueries();
        List<QueryResult<Object>> appliedRecords =
                executors.get(new AliasEnv(databaseAlias, EnvManager.currentEnv()))
                        .executeQueries(queries);
        return new StorageOperationResult(appliedRecords);
    }

    @Override
    public void clearSystem() {
        executors.forEach((aliasEnv, sqlExecutor) -> {
            if (isTruncate(integrationType, aliasEnv)
                    && Objects.equals(aliasEnv.getEnvironment(), EnvManager.currentEnv())) {
                sqlExecutor.truncate();
            }
        });
    }
}
