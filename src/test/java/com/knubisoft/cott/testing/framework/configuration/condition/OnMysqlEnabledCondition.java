package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Mysql;
import com.knubisoft.cott.testing.model.global_config.MysqlIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMysqlEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {

        final MysqlIntegration mysqlIntegration =
                GlobalTestConfigurationProvider.getDefaultIntegration().getMysqlIntegration();
        if (Objects.nonNull(mysqlIntegration)) {
            return mysqlIntegration.getMysql()
                    .stream().anyMatch(Mysql::isEnabled);
        }
        return false;
    }
}
