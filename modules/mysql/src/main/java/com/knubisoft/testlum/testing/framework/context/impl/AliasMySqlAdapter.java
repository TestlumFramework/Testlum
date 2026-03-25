package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.constant.DelimiterConstant;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.db.AbstractStorageOperation;
import com.knubisoft.testlum.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import com.knubisoft.testlum.testing.model.scenario.StorageName;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

@Conditional({OnMysqlEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasMySqlAdapter implements AliasAdapter {

    private final MySqlOperation mySqlOperation;
    private final Integrations integrations;

    @Override
    public void apply(final Map<String, AbstractStorageOperation> aliasMap) {
        for (Mysql mysql : integrations.getMysqlIntegration().getMysql()) {
            if (mysql.isEnabled()) {
                aliasMap.put(StorageName.MYSQL + DelimiterConstant.UNDERSCORE + mysql.getAlias(), mySqlOperation);
            }
        }
    }
}
