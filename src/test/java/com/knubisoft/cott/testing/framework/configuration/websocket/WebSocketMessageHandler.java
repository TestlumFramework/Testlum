package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.exception.DefaultFrameworkException;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.stomp.StompFrameHandler;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.util.MimeType;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;

public class WebSocketMessageHandler implements StompFrameHandler {

    private final List<String> messages = new ArrayList<>();
    private CountDownLatch messagesLatch = new CountDownLatch(0);


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
            messages.add((String) payload);
        } else if (payload instanceof byte[]) {
            messages.add(new String((byte[]) payload));
        } else {
            throw new DefaultFrameworkException("Unknown content type received");
        }
        messagesLatch.countDown();
    }

    public List<String> getReceivedMessages() {
        return messages;
    }

    public CountDownLatch getMessagesLatch(final int messagesNumber) {
        if (messages.size() < messagesNumber) {
            messagesLatch = new CountDownLatch(messagesNumber - messages.size());
        }
        return messagesLatch;
    }
}
