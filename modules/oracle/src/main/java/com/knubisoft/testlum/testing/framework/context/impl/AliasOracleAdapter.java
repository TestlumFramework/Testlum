package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnOracleEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.OracleOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Oracle;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnOracleEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasOracleAdapter implements AliasAdapter {

    private final OracleOperation oracleOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Oracle oracle : integrations.getOracleIntegration().getOracle()) {
            if (oracle.isEnabled()) {
                aliasMap.put(StorageName.ORACLE + DelimiterConstant.UNDERSCORE + oracle.getAlias(), oracleOperation);
            }
        }
    }
}
