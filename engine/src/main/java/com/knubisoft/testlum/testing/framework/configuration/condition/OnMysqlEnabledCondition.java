package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMysqlEnabledCondition implements Condition {

    private final MysqlIntegration mysqlIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getMysqlIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(mysqlIntegration)) {
            return IntegrationsUtil.isEnabled(mysqlIntegration.getMysql());
        }
        return false;
    }
}
