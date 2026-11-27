package com.testlum.testing.framework.context.impl;

import com.testlum.testing.framework.condition.OnSqlDatabaseEnableCondition;
import com.testlum.testing.framework.context.AbstractAliasAdapter;
import com.testlum.testing.framework.db.sql.SqlDatabaseOperation;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnSqlDatabaseEnableCondition.class})
@Component
public class AliasSqlDatabaseAdapter extends AbstractAliasAdapter {

    public AliasSqlDatabaseAdapter(final SqlDatabaseOperation sqlDatabaseOperation,
                                   final Integrations integrations) {
        super(sqlDatabaseOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getSqlDatabaseIntegration().getSqlDatabase();
    }

    @Override
    protected String getStorageName() {
        return StorageName.SQLDATABASE.value();
    }
}
