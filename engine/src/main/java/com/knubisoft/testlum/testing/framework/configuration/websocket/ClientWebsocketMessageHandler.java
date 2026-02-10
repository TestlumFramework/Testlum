package com.knubisoft.testlum.testing.framework.configuration.websocket;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.AbstractWebSocketHandler;

import java.util.LinkedList;

import static com.knubisoft.testlum.testing.framework.constant.ExceptionMessage.UNEXPECTED_WEBSOCKET_MESSAGE_TYPE;

@Slf4j
public class ClientWebsocketMessageHandler extends AbstractWebSocketHandler {

    private final LinkedList<String> textMessages = new LinkedList<>();

    public LinkedList<String> getReceivedMessages() {
        return textMessages;
    }

    @Override
    public void handleMessage(final WebSocketSession session, final WebSocketMessage<?> message) {
        if (message instanceof TextMessage) {
            handleTextMessage(session, (TextMessage) message);
        } else {
            throw new DefaultFrameworkException(UNEXPECTED_WEBSOCKET_MESSAGE_TYPE, message);
        }
    }

    protected void handleTextMessage(final WebSocketSession session, final TextMessage message) {
        textMessages.addLast(message.getPayload());
    }

    @Override
    public void handleTransportError(final WebSocketSession session, final Throwable exception) {
        throw new DefaultFrameworkException(exception);
    }
}
