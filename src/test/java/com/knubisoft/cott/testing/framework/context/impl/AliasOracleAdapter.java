package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.sql.OracleOperation;
import com.knubisoft.cott.testing.model.global_config.Oracle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.ORACLE;

@Conditional({OnOracleEnabledCondition.class})
@Component
public class AliasOracleAdapter implements AliasAdapter {

    @Autowired(required = false)
    private OracleOperation oracleOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getOracleIntegration().getOracle(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Oracle> oracleList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Oracle oracle : oracleList) {
            if (oracle.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + ORACLE + UNDERSCORE + oracle.getAlias(), getMetadataOracle(oracle));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataOracle(final Oracle oracle) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(oracle)
                .storageOperation(oracleOperation)
                .build();
    }
}
