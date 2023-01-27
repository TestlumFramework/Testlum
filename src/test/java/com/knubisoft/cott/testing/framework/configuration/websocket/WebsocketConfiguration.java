package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnWebsocketEnabledCondition;
import com.knubisoft.cott.testing.framework.constant.DelimiterConstant;
import com.knubisoft.cott.testing.model.global_config.WebsocketApi;
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

@Configuration
@Conditional(OnWebsocketEnabledCondition.class)
public class WebsocketConfiguration {

    @Bean
    public Map<String, WebsocketConnectionManager> websocketConnectionSupplier() {
        final Map<String, WebsocketConnectionManager> connectionSupplierMap = new HashMap<>();
        GlobalTestConfigurationProvider.getIntegrations()
                .forEach((s, integrations) -> addWebsocketConnection(s,
                        integrations.getWebsockets().getApi(), connectionSupplierMap));
        return connectionSupplierMap;
    }

    private void addWebsocketConnection(final String envName,
                                        final List<WebsocketApi> websocketApis,
                                        final Map<String, WebsocketConnectionManager> connectionSupplierMap) {
        for (WebsocketApi websocket : websocketApis) {
            if (websocket.isStomp()) {
                connectionSupplierMap.put(envName + DelimiterConstant.UNDERSCORE + websocket.getAlias(),
                        getWsStompConnectionManager(websocket.getUrl()));
            } else {
                connectionSupplierMap.put(envName + DelimiterConstant.UNDERSCORE + websocket.getAlias(),
                        getWsStandardConnectionManager(websocket.getUrl()));
            }
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
