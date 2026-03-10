package com.knubisoft.testlum.testing.framework.condition;

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
    protected Optional<List<? extends Integration>> getIntegrations(final Optional<Integrations> integrations) {
        return integrations.map(Integrations::getWebsockets).map(Websockets::getApi);
    }
}
