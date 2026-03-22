package com.knubisoft.testlum.testing.framework.configuration.websocket;

import com.knubisoft.testlum.testing.connection.ConnectionTemplate;
import com.knubisoft.testlum.testing.connection.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.GlobalTestConfigurationProvider;
import com.knubisoft.testlum.testing.framework.condition.OnWebsocketEnabledCondition;
import com.knubisoft.testlum.testing.framework.env.AliasEnv;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.interpreter.WebsocketConnectionManager;
import com.knubisoft.testlum.testing.model.global_config.Integrations;
import com.knubisoft.testlum.testing.model.global_config.WebsocketApi;
import com.knubisoft.testlum.testing.model.global_config.WebsocketProtocol;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;

@Configuration
@Conditional(OnWebsocketEnabledCondition.class)
@RequiredArgsConstructor
public class WebsocketConfiguration {

    private final ConnectionTemplate connectionTemplate;

    @Bean
    public Map<AliasEnv, WebsocketConnectionManager> websocketConnectionSupplier(
            final GlobalTestConfigurationProvider.EnvToIntegrationMap envTointegrations
    ) {
        final Map<AliasEnv, WebsocketConnectionManager> connectionSupplierMap = new HashMap<>();
        envTointegrations
                .forEach((env, integrations) -> addWebsocketConnection(integrations, env, connectionSupplierMap));
        return connectionSupplierMap;
    }

    private void addWebsocketConnection(final Integrations integrations, final String env,
                                        final Map<AliasEnv, WebsocketConnectionManager> connectionSupplierMap) {
        for (WebsocketApi websocket : integrations.getWebsockets().getApi()) {
            if (websocket.isEnabled()) {
                AliasEnv aliasEnv = new AliasEnv(websocket.getAlias(), env);
                WebsocketConnectionManager manager = getWebsocketConnectionManager(websocket);
                connectionSupplierMap.put(aliasEnv, manager);
            }
        }
    }

    private WebsocketConnectionManager getWebsocketConnectionManager(final WebsocketApi websocket) {
        return connectionTemplate.executeWithRetry(
                String.format(LogMessage.CONNECTION_INTEGRATION_DATA, "Websocket", websocket.getAlias()), () -> {
                    if (WebsocketProtocol.STOMP == websocket.getProtocol()) {
                        return getWsStompConnectionManager(websocket.getUrl());
                    } else {
                        return getWsStandardConnectionManager(websocket.getUrl());
                    }
                },
                forWebSocket(websocket)
        );
    }

    private IntegrationHealthCheck<WebsocketConnectionManager> forWebSocket(final WebsocketApi websocket) {
        return wsManager -> {
            try {
                wsManager.openConnection();
                if (!wsManager.isConnected()) {
                    throw new DefaultFrameworkException("failed to connect");
                }
            } catch (Exception e) {
                closeConnectionManager(wsManager);
                throw new DefaultFrameworkException(
                        "handshake failed for - " + websocket.getUrl() + " " + e.getMessage());
            }
        };
    }

    private void closeConnectionManager(final WebsocketConnectionManager wsManager) {
        try {
            wsManager.closeConnection();
        } catch (Exception ex) {
            throw new DefaultFrameworkException(ex.getMessage());
        }
    }

    private WebsocketStandardConnectionManager getWsStandardConnectionManager(final String url) {
        return new WebsocketStandardConnectionManager(
                new StandardWebSocketClient(), new ClientWebsocketMessageHandler(), url);
    }


    private WebsocketStompConnectionManager getWsStompConnectionManager(final String url) {
        return new WebsocketStompConnectionManager(getWebsocketStompClient(), new ClientStompSessionHandler(), url);
    }

    private WebSocketStompClient getWebsocketStompClient() {
        WebSocketStompClient stompClient = new WebSocketStompClient(new StandardWebSocketClient());
        List<MessageConverter> messageConverters = Arrays.asList(
                new MappingJackson2MessageConverter(),
                new StringMessageConverter());
        stompClient.setMessageConverter(new CompositeMessageConverter(messageConverters));
        return stompClient;
    }
}
