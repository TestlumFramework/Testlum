package com.testlum.testing.framework.context.impl;

import com.testlum.testing.framework.condition.OnClickhouseEnabledCondition;
import com.testlum.testing.framework.context.AbstractAliasAdapter;
import com.testlum.testing.framework.db.sql.ClickhouseOperation;
import com.testlum.testing.model.global_config.Integration;
import com.testlum.testing.model.global_config.Integrations;
import com.testlum.testing.model.scenario.StorageName;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;

@Conditional({OnClickhouseEnabledCondition.class})
@Component
public class AliasClickhouseAdapter extends AbstractAliasAdapter {

    public AliasClickhouseAdapter(final ClickhouseOperation clickhouseOperation,
                                  final Integrations integrations) {
        super(clickhouseOperation, integrations);
    }

    @Override
    protected List<? extends Integration> getIntegrationList(final Integrations integrations) {
        return integrations.getClickhouseIntegration().getClickhouse();
    }

    @Override
    protected String getStorageName() {
        return StorageName.CLICKHOUSE.value();
    }
}
