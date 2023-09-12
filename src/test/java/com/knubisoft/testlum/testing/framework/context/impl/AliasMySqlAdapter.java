package com.knubisoft.testlum.testing.framework.context.impl;

import com.knubisoft.testlum.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.context.AliasAdapter;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.testlum.testing.framework.context.NameToAdapterAliasImpl;
import com.knubisoft.testlum.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.testlum.testing.model.global_config.Mysql;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.testlum.testing.model.scenario.StorageName.MYSQL;

@Conditional({OnMysqlEnabledCondition.class})
@Component
public class AliasMySqlAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MySqlOperation mySqlOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Mysql mysql : ConfigProvider.getDefaultIntegrations().getMysqlIntegration().getMysql()) {
            if (mysql.isEnabled()) {
                aliasMap.put(MYSQL + UNDERSCORE + mysql.getAlias(), getMetadataMySQL(mysql));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataMySQL(final Mysql mysql) {
        return NameToAdapterAliasImpl.Metadata.builder()
                .configuration(mysql)
                .storageOperation(mySqlOperation)
                .build();
    }
}
