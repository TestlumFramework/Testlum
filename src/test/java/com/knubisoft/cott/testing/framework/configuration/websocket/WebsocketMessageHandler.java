package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.util.MimeType;

import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.Objects;

public class WebsocketMessageHandler implements StompFrameHandler {

    private final LinkedList<String> messages = new LinkedList<>();


    @Override
    public Type getPayloadType(final StompHeaders headers) {
        MimeType mimeType = headers.getContentType();
        if (Objects.isNull(mimeType) || mimeType.isCompatibleWith(MediaType.TEXT_PLAIN)) {
            return String.class;
        } else if (mimeType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return byte[].class;
        }
        throw new DefaultFrameworkException("Unknown content type received");
    }

    @Override
    public void handleFrame(final StompHeaders headers, final Object payload) {
        if (payload instanceof String) {
            messages.addLast((String) payload);
        } else if (payload instanceof byte[]) {
            messages.addLast(new String((byte[]) payload));
        } else {
            throw new DefaultFrameworkException("Unknown content type received");
        }
    }

    public LinkedList<String> getReceivedMessages() {
        return messages;
    }
}
