package com.knubisoft.e2e.testing.framework.configuration.condition;

import com.knubisoft.e2e.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.e2e.testing.model.global_config.Mysql;
import com.knubisoft.e2e.testing.model.global_config.Mysqls;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMysqlEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext,
                           final AnnotatedTypeMetadata annotatedTypeMetadata) {

        final Mysqls mysqls = GlobalTestConfigurationProvider.provide().getMysqls();
        if (Objects.nonNull(mysqls)) {
            return mysqls.getMysql()
                    .stream().anyMatch(Mysql::isEnabled);
        }
        return false;
    }
}
