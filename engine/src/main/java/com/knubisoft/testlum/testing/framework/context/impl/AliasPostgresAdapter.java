package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.POSTGRES;

@Conditional({OnPostgresEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasPostgresAdapter implements AliasAdapter {

    private final PostgresSqlOperation postgresSqlOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Postgres postgres : integrations.getPostgresIntegration().getPostgres()) {
            if (postgres.isEnabled()) {
                aliasMap.put(POSTGRES + UNDERSCORE + postgres.getAlias(), postgresSqlOperation);
            }
        }
    }
}
