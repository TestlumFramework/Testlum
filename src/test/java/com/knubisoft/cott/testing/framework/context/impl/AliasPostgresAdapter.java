package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnPostgresEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.sql.PostgresSqlOperation;
import com.knubisoft.cott.testing.model.global_config.Postgres;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.POSTGRES;

@Conditional({OnPostgresEnabledCondition.class})
@Component
public class AliasPostgresAdapter implements AliasAdapter {

    @Autowired(required = false)
    private PostgresSqlOperation postgresSqlOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getPostgresIntegration().getPostgres(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Postgres> postgresList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Postgres postgres : postgresList) {
            if (postgres.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + POSTGRES + UNDERSCORE + postgres.getAlias(),
                        getMetadataPostgres(postgres));
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
