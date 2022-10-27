package com.knubisoft.cott.testing.framework.configuration.websocket;

import com.knubisoft.cott.testing.framework.util.LogUtil;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

public class GlobalStompSessionHandler extends StompSessionHandlerAdapter {

    @Override
    public void handleTransportError(final StompSession session, final Throwable exception) {
        LogUtil.logException((Exception) exception);
    }

    @Override
    public void handleException(final StompSession s,
                                final StompCommand c,
                                final StompHeaders h,
                                final byte[] p,
                                final Throwable ex) {
        LogUtil.logException((Exception) ex);
    }
}
