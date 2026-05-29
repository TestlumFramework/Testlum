package com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;

public class InboundExchangeOutput {

    private final InboundResponse responseToSender;
    private final InboundResponse responseFromDestination;

    public InboundExchangeOutput(InboundResponse responseToSender, InboundResponse responseFromDestination) {
        this.responseToSender = responseToSender;
        this.responseFromDestination = responseFromDestination;
    }

    public static InboundExchangeOutput mock(InboundResponse mock) {
        return new InboundExchangeOutput(mock, null);
    }

    public static InboundExchangeOutput forwarded(InboundResponse responseFromDestination) {
        return new InboundExchangeOutput(responseFromDestination, responseFromDestination);
    }

    public static InboundExchangeOutput modifyForwarded(InboundResponse responseFromDestination,
                                                        InboundResponse modifiedResponse) {
        return new InboundExchangeOutput(modifiedResponse, responseFromDestination);
    }

    public InboundResponse getResponseToSender() {
        return responseToSender;
    }

    public InboundResponse getResponseFromDestination() {
        return responseFromDestination;
    }
}
