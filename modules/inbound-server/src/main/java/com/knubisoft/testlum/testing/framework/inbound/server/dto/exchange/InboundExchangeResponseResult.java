package com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;

public class InboundExchangeResponseResult {

    private final InboundResponse responseToSender;
    private final InboundResponse responseFromDestination;

    public InboundExchangeResponseResult(InboundResponse responseToSender, InboundResponse responseFromDestination) {
        this.responseToSender = responseToSender;
        this.responseFromDestination = responseFromDestination;
    }

    public static InboundExchangeResponseResult mock(InboundResponse mock) {
        return new InboundExchangeResponseResult(mock, null);
    }

    public static InboundExchangeResponseResult forwarded(InboundResponse responseFromDestination) {
        return new InboundExchangeResponseResult(responseFromDestination, responseFromDestination);
    }

    public static InboundExchangeResponseResult modifyForwarded(InboundResponse responseFromDestination,
                                                                InboundResponse modifiedResponse) {
        return new InboundExchangeResponseResult(modifiedResponse, responseFromDestination);
    }

    public InboundResponse getResponseToSender() {
        return responseToSender;
    }

    public InboundResponse getResponseFromDestination() {
        return responseFromDestination;
    }
}
