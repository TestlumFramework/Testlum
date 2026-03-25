package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnClickhouseEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.testlum.testing.model.global_config.Clickhouse;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

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
                aliasMap.put(StorageName.CLICKHOUSE
                        + DelimiterConstant.UNDERSCORE + clickhouse.getAlias(), clickhouseOperation);
            }
        }
    }
}
