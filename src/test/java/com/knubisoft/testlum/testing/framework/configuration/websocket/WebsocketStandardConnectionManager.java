package com.knubisoft.testlum.testing.framework.configuration.websocket;

import com.knubisoft.testlum.testing.framework.interpreter.WebsocketConnectionManager;
import com.knubisoft.testlum.testing.model.scenario.WebsocketReceive;
import com.knubisoft.testlum.testing.model.scenario.WebsocketSend;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.LinkedList;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WEBSOCKET_CONNECTION_CLOSED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.WEBSOCKET_CONNECTION_ESTABLISHED;
import static java.util.Objects.nonNull;

@Slf4j
@RequiredArgsConstructor
public class WebsocketStandardConnectionManager implements WebsocketConnectionManager {

    private final WebSocketClient websocketClient;
    private final ClientWebsocketMessageHandler websocketMessageHandler;
    private final String url;

    private WebSocketSession websocketSession;

    @Override
    public void sendMessage(final WebsocketSend wsSend, final String payload) throws IOException {
        if (nonNull(websocketSession)) {
            websocketSession.sendMessage(new TextMessage(payload));
        }
    }

    @Override
    public LinkedList<String> receiveMessages(final WebsocketReceive wsReceive) {
        return websocketMessageHandler.getReceivedMessages();
    }

    @Override
    public void openConnection() throws Exception {
        ListenableFuture<WebSocketSession> connection = websocketClient.doHandshake(
                websocketMessageHandler,
                new WebSocketHttpHeaders(),
                UriComponentsBuilder.fromUriString(url).build().toUri());

        websocketSession = connection.get();
        log.info(WEBSOCKET_CONNECTION_ESTABLISHED, websocketSession.getId());
    }

    @Override
    public void closeConnection() throws Exception {
        if (nonNull(websocketSession)) {
            websocketSession.close();
            log.info(WEBSOCKET_CONNECTION_CLOSED, websocketSession.getId());
        }
    }

    @Override
    public boolean isConnected() {
        return nonNull(websocketSession) && websocketSession.isOpen();
    }

    @Override
    public void subscribeTo(final String topic) {
        throw new UnsupportedOperationException(topic);
    }
}
