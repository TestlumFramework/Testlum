package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnPostgresEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Postgres;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

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
                aliasMap.put(StorageName.POSTGRES
                        + DelimiterConstant.UNDERSCORE + postgres.getAlias(), postgresSqlOperation);
            }
        }
    }
}
