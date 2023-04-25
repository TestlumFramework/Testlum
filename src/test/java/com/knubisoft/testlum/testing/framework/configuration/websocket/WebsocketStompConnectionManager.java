package com.knubisoft.testlum.testing.framework.configuration.websocket;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_CLOSED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_ESTABLISHED;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class WebsocketStompConnectionManager implements WebsocketConnectionManager {

    private final WebSocketStompClient websocketStompClient;
    private final ClientStompSessionHandler websocketSessionHandler;
    private final String url;

    private final Map<String, WebsocketStompMessageHandler> topicToMessageHandler = new HashMap<>();
    private StompSession stompSession;

    @Override
    public void sendMessage(final WebsocketSend wsSend, final String payload) {
        if (nonNull(stompSession)) {
            stompSession.send(wsSend.getEndpoint(), payload);
        }
    }

    @Override
    public LinkedList<String> receiveMessages(final WebsocketReceive wsReceive) {
        WebsocketStompMessageHandler messageHandler = topicToMessageHandler.get(wsReceive.getTopic());
        if (nonNull(messageHandler)) {
            return messageHandler.getReceivedMessages();
        }
        throw new DefaultFrameworkException(WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND, wsReceive.getTopic());
    }

    @Override
    public void subscribeTo(final String topic) {
        WebsocketStompMessageHandler messageHandler = new WebsocketStompMessageHandler();
        topicToMessageHandler.put(topic, messageHandler);
        //todo save Subscription if 'unsubscribe' command is needed
        if (nonNull(stompSession)) {
            stompSession.subscribe(topic, messageHandler);
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
