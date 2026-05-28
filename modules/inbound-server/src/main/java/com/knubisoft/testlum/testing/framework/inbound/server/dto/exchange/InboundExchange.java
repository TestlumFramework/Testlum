package com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;

public class InboundExchange {

    private final InboundExchangeStatus status;
    private final InboundRequest receivedRequest;
    private final InboundResponse sendToSenderResponse;
    private final InboundResponse responseFromDestination;
    private final String errorMessage;

    public InboundExchange(InboundExchangeStatus status, InboundRequest receivedRequest, InboundResponse sendToSenderResponse, InboundResponse responseFromDestination, String errorMessage) {
        this.status = status;
        this.receivedRequest = receivedRequest;
        this.sendToSenderResponse = sendToSenderResponse;
        this.responseFromDestination = responseFromDestination;
        this.errorMessage = errorMessage;
    }

    public static InboundExchange received(InboundRequest request, InboundExchangeResponseResult outcome) {
        return new InboundExchange(InboundExchangeStatus.RECEIVED, request,
                outcome.getResponseToSender(), outcome.getResponseFromDestination(), null);
    }

    public static InboundExchange timeout() {
        return new InboundExchange(InboundExchangeStatus.TIMEOUT, null, null, null, null);
    }

    public static InboundExchange error(InboundRequest request, String errorMessage) {
        return new InboundExchange(InboundExchangeStatus.ERROR, request, null, null, errorMessage);
    }

    public InboundExchangeStatus getStatus() {
        return status;
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

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isReceived() {
        return status == InboundExchangeStatus.RECEIVED;
    }

}
