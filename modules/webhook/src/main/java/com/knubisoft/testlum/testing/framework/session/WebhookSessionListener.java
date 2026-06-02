package com.knubisoft.testlum.testing.framework.session;

import com.knubisoft.testing.framework.session.SessionEvent;
import com.knubisoft.testing.framework.session.SessionListener;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;

import java.util.concurrent.BlockingQueue;

public class WebhookSessionListener implements SessionListener {

    private final BlockingQueue<SessionEvent> events;

    public WebhookSessionListener(BlockingQueue<SessionEvent> events) {
        this.events = events;
    }

    @Override
    public void onRequestReceived(int index, InboundRequest request, InboundExchangeOutput output) {
        events.offer(new SessionEvent.Received(index, request, output));
    }

    @Override
    public void onComplete() {
        events.offer(new SessionEvent.Completed());
    }

    @Override
    public void onFailure(InboundExchangeStatus status, String errorMessage, InboundRequest extraIfPresent) {
        events.offer(new SessionEvent.Failed(status, errorMessage, extraIfPresent));
    }
}
