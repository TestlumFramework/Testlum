package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.util.ConfigUtil;
import com.knubisoft.cott.testing.model.global_config.Websockets;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnWebsocketEnabledCondition implements Condition {

    private final Websockets websockets = GlobalTestConfigurationProvider.getDefaultIntegration().getWebsockets();

    @Override
    public boolean matches(final ConditionContext context, final AnnotatedTypeMetadata metadata) {
        if (Objects.nonNull(websockets)) {
            return ConfigUtil.isIntegrationEnabled(websockets.getApi());
        }
        return false;
    }
}
