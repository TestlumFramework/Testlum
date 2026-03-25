package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnSqlDatabaseEnableCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.SqlDatabaseOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.SqlDatabase;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnSqlDatabaseEnableCondition.class})
@Component
@RequiredArgsConstructor
public class AliasSqlDatabaseAdapter implements AliasAdapter {

    private final SqlDatabaseOperation sqlDatabaseOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (SqlDatabase database : integrations.getSqlDatabaseIntegration().getSqlDatabase()) {
            if (database.isEnabled()) {
                aliasMap.put("SQLDATABASE" + DelimiterConstant.UNDERSCORE + database.getAlias(), sqlDatabaseOperation);
            }
        }
    }
}