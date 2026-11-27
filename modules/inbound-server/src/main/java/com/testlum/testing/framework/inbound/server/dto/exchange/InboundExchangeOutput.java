package com.testlum.testing.framework.inbound.server.dto.exchange;

import com.testlum.testing.framework.inbound.server.dto.InboundResponse;

public class InboundExchangeOutput {

    private final InboundResponse responseToSender;
    private final InboundResponse responseFromDestination;

    public InboundExchangeOutput(final InboundResponse responseToSender,
                                 final InboundResponse responseFromDestination) {
        this.responseToSender = responseToSender;
        this.responseFromDestination = responseFromDestination;
    }

    public static InboundExchangeOutput mock(final InboundResponse mock) {
        return new InboundExchangeOutput(mock, null);
    }

    public static InboundExchangeOutput forwarded(final InboundResponse responseFromDestination) {
        return new InboundExchangeOutput(responseFromDestination, responseFromDestination);
    }

    public static InboundExchangeOutput modifyForwarded(final InboundResponse responseFromDestination,
                                                        final InboundResponse modifiedResponse) {
        return new InboundExchangeOutput(modifiedResponse, responseFromDestination);
    }

    public InboundResponse getResponseToSender() {
        return responseToSender;
    }

    public InboundResponse getResponseFromDestination() {
        return responseFromDestination;
    }
}
