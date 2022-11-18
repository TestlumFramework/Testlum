package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnWebsocketEnabledCondition;
import com.knubisoft.cott.testing.model.global_config.WebSocket;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.CompositeMessageConverter;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.converter.MessageConverter;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Configuration
@Conditional({OnWebsocketEnabledCondition.class})
public class WebsocketConfiguration {

    @Bean
    public Map<String, WebsocketConnectionSupplier> websocketConnections() {
        final Map<String, WebsocketConnectionSupplier> connectionSupplierMap = new HashMap<>();
        for (WebSocket websocket : GlobalTestConfigurationProvider.getIntegrations().getWebsockets().getWebsocket()) {
            WebSocketStompClient websocketClient = createWebsocketStompClient();
            connectionSupplierMap.put(websocket.getAlias(),
                    () -> websocketClient.connect(websocket.getUrl(), new GlobalStompSessionHandler())
            );
        }
        return connectionSupplierMap;
    }

    private WebSocketStompClient createWebsocketStompClient() {
        WebSocketClient sockJsClient = getSockJsWebsocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        List<MessageConverter> messageConverters = Arrays.asList(
                new MappingJackson2MessageConverter(),
                new StringMessageConverter()
        );
        stompClient.setMessageConverter(new CompositeMessageConverter(messageConverters));
        return stompClient;
    }

    private WebSocketClient getSockJsWebsocketClient() {
        WebSocketClient standardWebsocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(standardWebsocketClient));
        return new SockJsClient(transports);
    }

    public interface WebsocketConnectionSupplier extends Supplier<ListenableFuture<StompSession>> { }
}
