package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnOracleEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.sql.OracleOperation;
import com.knubisoft.e2e.testing.model.global_config.Oracle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.e2e.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.e2e.testing.model.scenario.StorageName.ORACLE;

@Conditional({OnOracleEnabledCondition.class})
@Component
public class AliasOracleAdapter implements AliasAdapter {

    @Autowired(required = false)
    private OracleOperation oracleOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Oracle oracle : GlobalTestConfigurationProvider.getIntegrations().getOracleIntegration().getOracle()) {
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
