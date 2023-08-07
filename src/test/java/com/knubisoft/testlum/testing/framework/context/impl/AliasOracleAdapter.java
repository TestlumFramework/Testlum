package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.db.sql.OracleOperation;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.ORACLE;

@Conditional({OnOracleEnabledCondition.class})
@Component
public class AliasOracleAdapter implements AliasAdapter {

    @Autowired(required = false)
    private OracleOperation oracleOperation;
    @Autowired
    private GlobalTestConfigurationProvider globalTestConfigurationProvider;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Oracle oracle
                : globalTestConfigurationProvider.getDefaultIntegrations().getOracleIntegration().getOracle()) {
            if (oracle.isEnabled()) {
                aliasMap.put(ORACLE + UNDERSCORE + oracle.getAlias(), getMetadataOracle(oracle));
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
