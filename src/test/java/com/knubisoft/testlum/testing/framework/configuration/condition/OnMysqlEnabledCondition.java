package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.MysqlIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnMysqlEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        GlobalTestConfigurationProvider configurationProvider =
                context.getBeanFactory().getBean(GlobalTestConfigurationProvider.class);
        MysqlIntegration mysqlIntegration = configurationProvider.getDefaultIntegrations().getMysqlIntegration();
        if (Objects.nonNull(mysqlIntegration)) {
            return IntegrationsUtil.isEnabled(mysqlIntegration.getMysql());
        }
        return false;
    }
}
