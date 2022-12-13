package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.util.MimeType;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Objects;

import static com.knubisoft.cott.testing.framework.constant.ExceptionMessage.UNEXPECTED_WEBSOCKET_MESSAGE_TYPE;

public class WebsocketMessageHandler implements StompFrameHandler {

    private final LinkedList<String> messages = new LinkedList<>();

    public LinkedList<String> getReceivedMessages() {
        return messages;
    }

    @Override
    public Type getPayloadType(final StompHeaders headers) {
        boolean isTextType = isPayloadTextType(headers.getContentType());
        if (isTextType) {
            return String.class;
        } else {
            return byte[].class;
        }
    }

    @Override
    public void handleFrame(final StompHeaders headers, final Object payload) {
        boolean isTextType = isPayloadTextType(headers.getContentType());
        if (isTextType) {
            messages.addLast((String) payload);
        } else {
            messages.addLast(new String((byte[]) payload));
        }
    }

    private boolean isPayloadTextType(final MimeType mimeType) {
        if (Objects.isNull(mimeType) || mimeType.isCompatibleWith(MediaType.TEXT_PLAIN)) {
            return true;
        } else if (mimeType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return false;
        }
        throw new DefaultFrameworkException(UNEXPECTED_WEBSOCKET_MESSAGE_TYPE, mimeType);
    }
}
