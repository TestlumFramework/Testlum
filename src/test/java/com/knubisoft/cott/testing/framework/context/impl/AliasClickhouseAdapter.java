package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnClickhouseEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.sql.ClickhouseOperation;
import com.knubisoft.cott.testing.model.global_config.Clickhouse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.CLICKHOUSE;

@Conditional({OnClickhouseEnabledCondition.class})
@Component
public class AliasClickhouseAdapter implements AliasAdapter {

    @Autowired(required = false)
    private ClickhouseOperation clickhouseOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Clickhouse clickhouse
                : GlobalTestConfigurationProvider.getDefaultIntegration().getClickhouseIntegration().getClickhouse()) {
            if (clickhouse.isEnabled()) {
                aliasMap.put(CLICKHOUSE + UNDERSCORE + clickhouse.getAlias(), getMetadataClickhouse(clickhouse));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataClickhouse(final Clickhouse clickhouse) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(clickhouse)
                .storageOperation(clickhouseOperation)
                .build();
    }
}
