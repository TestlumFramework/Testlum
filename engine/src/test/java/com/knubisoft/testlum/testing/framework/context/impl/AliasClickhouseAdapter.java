package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.ConfigProviderImpl.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.CLICKHOUSE;

@Conditional({OnClickhouseEnabledCondition.class})
@Component
public class AliasClickhouseAdapter implements AliasAdapter {

    @Autowired(required = false)
    private ClickhouseOperation clickhouseOperation;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Clickhouse clickhouse
                : GlobalTestConfigurationProvider.getDefaultIntegrations().getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                aliasMap.put(CLICKHOUSE + UNDERSCORE + clickhouse.getAlias(), clickhouseOperation);
            }
        }
    }
}
