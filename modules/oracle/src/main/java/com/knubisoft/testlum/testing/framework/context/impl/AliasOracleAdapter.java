package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnOracleEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.sql.OracleOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnOracleEnabledCondition.class})
@Component
public class AliasOracleAdapter extends AbstractAliasAdapter {

    public AliasOracleAdapter(final OracleOperation oracleOperation,
                              final Integrations integrations) {
        super(oracleOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getOracleIntegration().getOracle();
    }

    @Override
    protected String getStorageName() {
        return StorageName.ORACLE.value();
    }
}
