package com.knubisoft.testlum.testing.framework.inbound.dto;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.InboundSession;
import com.knubisoft.testlum.testing.framework.inbound.server.util.InboundResponseFactory;

import java.util.concurrent.atomic.AtomicReference;

public class TestSession implements InboundSession {
    private final String path;
    private final String label;
    private final AtomicReference<InboundRequest> request = new AtomicReference<>();

    public TestSession(String path, String label){
        this.path = path;
        this.label = label;
    }

    @Override
    public String endpoint() {
        return path;
    }

    public InboundResponse onRequest(InboundRequest r){
        request.set(r);
        return InboundResponseFactory.json(200, "{\"handledBy\":\""+label+"\"}");
    }

    @Override
    public InboundExchange awaitExchange() {
        return null;
    }

    @Override
    public boolean isCompleted() {
        return request.get()!=null;
    }

    public AtomicReference<InboundRequest> getRequest() {
        return request;
    }
}
