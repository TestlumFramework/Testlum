package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AbstractAliasAdapter;
import com.knubisoft.testlum.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnMysqlEnabledCondition.class})
@Component
public class AliasMySqlAdapter extends AbstractAliasAdapter {

    public AliasMySqlAdapter(final MySqlOperation mySqlOperation,
                             final Integrations integrations) {
        super(mySqlOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getMysqlIntegration().getMysql();
    }

    @Override
    protected String getStorageName() {
        return StorageName.MYSQL.value();
    }
}
