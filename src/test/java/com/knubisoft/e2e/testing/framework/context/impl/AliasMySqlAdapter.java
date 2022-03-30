package com.knubisoft.e2e.testing.framework.context.impl;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.framework.constant.DelimiterConstant;
import com.knubisoft.e2e.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.e2e.testing.framework.context.AliasAdapter;
import com.knubisoft.e2e.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.e2e.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.e2e.testing.model.global_config.Mysql;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.e2e.testing.model.scenario.StorageName.MYSQL;

@Conditional({OnMysqlEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasMySqlAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MySqlOperation mySqlOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Mysql mysql : GlobalTestConfigurationProvider.getIntegrations().getMysqls().getMysql()) {
            if (mysql.isEnabled()) {
                aliasMap.put(MYSQL + DelimiterConstant.UNDERSCORE + mysql.getAlias(), getMetadataMySQL(mysql));
            }
        }
    }

    private NameToAdapterAlias.Metadata getMetadataMySQL(final Mysql mysql) {
        return NameToAdapterAlias.Metadata.builder()
                .configuration(mysql)
                .storageOperation(mySqlOperation)
                .build();
    }
}
