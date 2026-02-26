package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.CLICKHOUSE;

@Conditional({OnClickhouseEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasClickhouseAdapter implements AliasAdapter {

    private final ClickhouseOperation clickhouseOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Clickhouse clickhouse : integrations.getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                aliasMap.put(CLICKHOUSE + UNDERSCORE + clickhouse.getAlias(), clickhouseOperation);
            }
        }
    }
}
