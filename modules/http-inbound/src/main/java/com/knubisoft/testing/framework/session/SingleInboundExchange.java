package com.knubisoft.testing.framework.session;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.AbstractInboundExchange;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;

public class SingleInboundExchange extends AbstractInboundExchange {

    private final InboundRequest receivedRequest;
    private final InboundResponse sendToSenderResponse;
    private final InboundResponse responseFromDestination;

    public SingleInboundExchange(InboundExchangeStatus status, InboundRequest receivedRequest, InboundResponse sendToSenderResponse, InboundResponse responseFromDestination, String errorMessage) {
        super(status, errorMessage);
        this.receivedRequest = receivedRequest;
        this.sendToSenderResponse = sendToSenderResponse;
        this.responseFromDestination = responseFromDestination;
    }

    public static SingleInboundExchange received(InboundRequest request, InboundExchangeOutput outcome) {
        return new SingleInboundExchange(InboundExchangeStatus.RECEIVED, request,
                outcome.getResponseToSender(), outcome.getResponseFromDestination(), null);
    }

    public static SingleInboundExchange timeout() {
        return new SingleInboundExchange(InboundExchangeStatus.TIMEOUT, null,
                null, null, null);
    }

    public static SingleInboundExchange error(InboundRequest request, String errorMessage) {
        return new SingleInboundExchange(InboundExchangeStatus.ERROR, request, null, null, errorMessage);
    }

    public InboundRequest getReceivedRequest() {
        return receivedRequest;
    }

    public InboundResponse getSendToSenderResponse() {
        return sendToSenderResponse;
    }

    public InboundResponse getResponseFromDestination() {
        return responseFromDestination;
    }

}
