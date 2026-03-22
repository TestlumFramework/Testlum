package com.knubisoft.testlum.testing.framework.configuration.websocket;

import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import com.knubisoft.testlum.testing.framework.interpreter.WebsocketConnectionManager;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

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
        if (Objects.nonNull(stompSession)) {
            stompSession.send(wsSend.getEndpoint(), payload);
        }
    }

    @Override
    public LinkedList<String> receiveMessages(final WebsocketReceive wsReceive) {
        WebsocketStompMessageHandler messageHandler = topicToMessageHandler.get(wsReceive.getTopic());
        if (Objects.nonNull(messageHandler)) {
            return messageHandler.getReceivedMessages();
        }
        log.info(LogMessage.WEBSOCKET_HANDLER_FOR_TOPIC_NOT_FOUND, wsReceive.getTopic());
        return new LinkedList<>();
    }

    @Override
    public void subscribeTo(final String topic) {
        //todo save Subscription if 'unsubscribe' command is needed
        boolean isSubscribed = topicToMessageHandler.containsKey(topic);
        if (isSubscribed) {
            log.info(LogMessage.WEBSOCKET_ALREADY_SUBSCRIBED, topic);
        } else if (Objects.nonNull(stompSession)) {
            WebsocketStompMessageHandler messageHandler = new WebsocketStompMessageHandler();
            stompSession.subscribe(topic, messageHandler);
            topicToMessageHandler.put(topic, messageHandler);
        }
    }

    @Override
    public void openConnection() throws Exception {
        CompletableFuture<StompSession> connection = websocketStompClient.connectAsync(url, websocketSessionHandler);
        stompSession = connection.get();
        log.info(LogMessage.WEBSOCKET_CONNECTION_ESTABLISHED, stompSession.getSessionId());
    }

    @Override
    public void closeConnection() {
        topicToMessageHandler.clear();
        if (isConnected()) {
            stompSession.disconnect();
            log.info(LogMessage.WEBSOCKET_CONNECTION_CLOSED, stompSession.getSessionId());
        } else if (!isConnected()) {
            log.info(LogMessage.UNABLE_TO_DISCONNECT_WEBSOCKET_BECAUSE_CLOSED);
        }
    }

    @Override
    public boolean isConnected() {
        return Objects.nonNull(stompSession) && stompSession.isConnected();
    }
}
