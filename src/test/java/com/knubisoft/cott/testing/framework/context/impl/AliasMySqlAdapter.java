package com.knubisoft.cott.testing.framework.context.impl;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnMysqlEnabledCondition;
import com.knubisoft.cott.testing.framework.context.AliasAdapter;
import com.knubisoft.cott.testing.framework.context.NameToAdapterAlias;
import com.knubisoft.cott.testing.framework.db.sql.MySqlOperation;
import com.knubisoft.cott.testing.model.global_config.Mysql;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.DelimiterConstant.UNDERSCORE;
import static com.knubisoft.cott.testing.model.scenario.StorageName.MYSQL;

@Conditional({OnMysqlEnabledCondition.class})
@Component
@RequiredArgsConstructor
public class AliasMySqlAdapter implements AliasAdapter {

    @Autowired(required = false)
    private MySqlOperation mySqlOperation;

    @Override
    public void apply(final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach(((s, integrations) -> addToAliasMap(s, integrations.getMysqlIntegration().getMysql(),
                        aliasMap)));
    }

    private void addToAliasMap(final String envName,
                               final List<Mysql> mysqlList,
                               final Map<String, NameToAdapterAlias.Metadata> aliasMap) {
        for (Mysql mysql : mysqlList) {
            if (mysql.isEnabled()) {
                aliasMap.put(envName + UNDERSCORE + MYSQL + UNDERSCORE + mysql.getAlias(), getMetadataMySQL(mysql));
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
