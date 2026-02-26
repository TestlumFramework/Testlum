package com.knubisoft.testlum.testing.framework.configuration.condition;

import com.knubisoft.testlum.testing.model.global_config.Integration;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.WebsocketApi;
import com.knubisoft.testlum.testing.model.global_config.Websockets;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class OnWebsocketEnabledCondition extends AbstractCondition<WebsocketApi> {

    @Override
    List<? extends Integration> getIntegrations(final Integrations integrations) {
        return Optional.ofNullable(integrations.getWebsockets())
                .map(Websockets::getApi)
                .orElse(null);
    }
}
