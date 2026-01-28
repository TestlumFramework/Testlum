package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.SqlDatabaseOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.framework.constant.MigrationConstant.SQL_DATABASE;

@Conditional({OnSqlDatabaseEnableCondition.class})
@Component
public class AliasSqlDatabaseAdapter implements AliasAdapter {

    @Autowired(required = false)
    private SqlDatabaseOperation sqlDatabaseOperation;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        Integrations integrations = ConfigProviderImpl.GlobalTestConfigurationProvider.getDefaultIntegrations();
        for (SqlDatabase database : integrations.getSqlDatabaseIntegration().getSqlDatabase()) {
            if (database.isEnabled()) {
                aliasMap.put(SQL_DATABASE + UNDERSCORE + database.getAlias(), sqlDatabaseOperation);
            }
        }
    }
}