package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.framework.configuration.condition.OnWebSocketEnabledCondition;
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
@Conditional({OnWebSocketEnabledCondition.class})
public class WebSocketConfiguration {

    @Bean
    public Map<String, WebSocketConnectionSupplier> webSocketConnections() {
        final Map<String, WebSocketConnectionSupplier> connectionSupplierMap = new HashMap<>();
        for (WebSocket webSocket : GlobalTestConfigurationProvider.getIntegrations().getWebSockets().getWebSocket()) {
            WebSocketStompClient webSocketClient = createWebSocketStompClient();
            connectionSupplierMap.put(webSocket.getAlias(),
                    () -> webSocketClient.connect(webSocket.getUrl(), new GlobalStompSessionHandler())
            );
        }
        return connectionSupplierMap;
    }

    private WebSocketStompClient createWebSocketStompClient() {
        WebSocketClient sockJsClient = getSockJsWebSocketClient();
        WebSocketStompClient stompClient = new WebSocketStompClient(sockJsClient);
        List<MessageConverter> messageConverters = Arrays.asList(
                new MappingJackson2MessageConverter(),
                new StringMessageConverter()
        );
        stompClient.setMessageConverter(new CompositeMessageConverter(messageConverters));
        return stompClient;
    }

    private WebSocketClient getSockJsWebSocketClient() {
        WebSocketClient standardWebSocketClient = new StandardWebSocketClient();
        List<Transport> transports = new ArrayList<>(1);
        transports.add(new WebSocketTransport(standardWebSocketClient));
        return new SockJsClient(transports);
    }

    public interface WebSocketConnectionSupplier extends Supplier<ListenableFuture<StompSession>> { }
}
