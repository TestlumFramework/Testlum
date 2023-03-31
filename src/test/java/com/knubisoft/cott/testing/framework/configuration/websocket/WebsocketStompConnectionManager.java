package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.cott.testing.model.scenario.WebsocketSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONNECTION_CLOSED;
import static com.knubisoft.cott.testing.framework.constant.LogMessage.CONNECTION_ESTABLISHED;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class WebsocketStompConnectionManager implements WebsocketConnectionManager {

    private final WebSocketStompClient websocketStompClient;
    private final ClientStompSessionHandler websocketSessionHandler;
    private final String url;

    private final Map<String, WebsocketMessageHandler> topicToMessageHandler = new HashMap<>();
    private StompSession stompSession;

    @Override
    public void sendMessage(final WebsocketSend wsSend, final String payload) {
        if (nonNull(stompSession)) {
            stompSession.send(wsSend.getEndpoint(), payload);
        }
    }

    @Override
    public LinkedList<String> receiveMessages(final String topic) {
        WebsocketMessageHandler websocketMessageHandler = topicToMessageHandler.get(topic);
        if (nonNull(websocketMessageHandler)) {
            return websocketMessageHandler.getReceivedMessages();
        }
        throw new DefaultFrameworkException(WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND, topic);
    }

    @Override
    public void subscribeTo(final String topic) {
        WebsocketMessageHandler handler = new WebsocketMessageHandler();
        topicToMessageHandler.put(topic, handler);
        //todo save Subscription if 'unsubscribe' command is needed
        if (nonNull(stompSession)) {
            stompSession.subscribe(topic, handler);
        }
    }

    @Override
    public void openConnection() throws Exception {
        ListenableFuture<StompSession> connection = websocketStompClient.connect(url, websocketSessionHandler);
        stompSession = connection.get();
        log.info(CONNECTION_ESTABLISHED, stompSession.getSessionId());
    }

    @Override
    public void closeConnection() {
        if (nonNull(stompSession)) {
            stompSession.disconnect();
            log.info(CONNECTION_CLOSED, stompSession.getSessionId());
        }
    }

    @Override
    public boolean isConnected() {
        return nonNull(stompSession) && stompSession.isConnected();
    }
}
