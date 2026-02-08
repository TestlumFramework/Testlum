package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.ClickhouseIntegration;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnClickhouseEnabledCondition implements Condition {

    private final ClickhouseIntegration clickhouseIntegration =
            GlobalTestConfigurationProvider.get().getDefaultIntegrations().getClickhouseIntegration();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(clickhouseIntegration)) {
            return IntegrationsUtil.isEnabled(clickhouseIntegration.getClickhouse());
        }
        return false;
    }
}
