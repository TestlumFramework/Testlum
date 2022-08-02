package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.cott.testing.model.global_config.Mysql;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.knubisoft.cott.testing.model.scenario.StorageName.MYSQL;

@Conditional({OnMysqlEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasMySqlAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MySqlOperation mySqlOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Mysql mysql : GlobalTestConfigurationProvider.getIntegrations().getMysqlIntegration().getMysql()) {
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
