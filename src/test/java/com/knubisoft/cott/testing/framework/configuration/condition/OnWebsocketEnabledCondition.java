package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.WebsocketApi;
import com.knubisoft.cott.testing.model.global_config.Websockets;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnWebsocketEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context,
                           final AnnotatedTypeMetadata metadata) {
        final Websockets websockets = GlobalTestConfigurationProvider.getDefaultIntegration().getWebsockets();
        if (Objects.nonNull(websockets)) {
            return websockets.getApi().stream().anyMatch(WebsocketApi::isEnabled);
        }
        return false;
    }
}
