package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.global.GlobalTestConfigurationProviderImpl.ConfigProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnClickhouseEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        ClickhouseIntegration clickhouseIntegration =
                ConfigProvider.getDefaultIntegrations().getClickhouseIntegration();
        if (Objects.nonNull(clickhouseIntegration)) {
            return IntegrationsUtil.isEnabled(clickhouseIntegration.getClickhouse());
        }
        return false;
    }
}
