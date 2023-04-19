package com.knubisoft.testlum.testing.framework.configuration.websocket;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class ClientStompSessionHandler extends StompSessionHandlerAdapter {

    @Override
    public void handleException(final StompSession session,
                                final StompCommand command,
                                final StompHeaders headers,
                                final byte[] payload,
                                final Throwable exception) {
        throw new DefaultFrameworkException(exception);
    }

    @Override
    public void handleTransportError(final StompSession session, final Throwable exception) {
        throw new DefaultFrameworkException(exception);
    }
}
