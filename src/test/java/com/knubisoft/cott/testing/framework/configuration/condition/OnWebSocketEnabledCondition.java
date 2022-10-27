package com.knubisoft.cott.testing.framework.configuration.condition;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.WebSocket;
import com.knubisoft.cott.testing.model.global_config.WebSockets;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

import java.util.Objects;

public class OnWebSocketEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext context,
                           final AnnotatedTypeMetadata metadata) {
        final WebSockets webSockets = GlobalTestConfigurationProvider.getIntegrations().getWebSockets();
        if (Objects.nonNull(webSockets)) {
            return webSockets.getWebSocket().stream().anyMatch(WebSocket::isEnabled);
        }
        return false;
    }
}
