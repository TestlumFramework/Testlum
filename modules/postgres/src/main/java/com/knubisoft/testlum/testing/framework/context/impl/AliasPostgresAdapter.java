package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnPostgresEnabledCondition.class})
@Component
public class AliasPostgresAdapter extends AbstractAliasAdapter {

    public AliasPostgresAdapter(final PostgresSqlOperation postgresSqlOperation,
                                final Integrations integrations) {
        super(postgresSqlOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getPostgresIntegration().getPostgres();
    }

    @Override
    protected String getStorageName() {
        return StorageName.POSTGRES.value();
    }
}
