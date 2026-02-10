package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.util.IntegrationsProviderImpl.IntegrationsUtil;
import com.knubisoft.testlum.testing.model.global_config.Websockets;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnWebsocketEnabledCondition implements Condition {

    private final Websockets websockets = GlobalTestConfigurationProvider.get().
            getDefaultIntegrations().getWebsockets();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(websockets)) {
            return IntegrationsUtil.isEnabled(websockets.getApi());
        }
        return false;
    }
}
