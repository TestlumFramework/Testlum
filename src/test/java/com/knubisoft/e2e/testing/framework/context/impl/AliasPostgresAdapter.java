package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.e2e.testing.model.global_config.Postgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.e2e.testing.model.scenario.StorageName.POSTGRES;

@Conditional({OnPostgresEnabledCondition.class})
@Component
public class AliasPostgresAdapter implements AliasAdapter {

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Postgres postgres
                : GlobalTestConfigurationProvider.getIntegrations().getPostgresIntegration().getPostgres()) {
            if (postgres.isEnabled()) {
                aliasMap.put(POSTGRES + UNDERSCORE + postgres.getAlias(), getMetadataPostgres(postgres));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataPostgres(final Postgres postgres) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(postgres)
                .storageOperation(postgresSqlOperation)
                .build();
    }
}
