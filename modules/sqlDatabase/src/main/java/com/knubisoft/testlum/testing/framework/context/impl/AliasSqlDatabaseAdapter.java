package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.sql.SqlDatabaseOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
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
