package com.knubisoft.testing.framework.exchange.producer;

import com.knubisoft.testing.framework.exchange.forwarder.DestinationForwarder;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeResponseResult;

public class ResponseProducerFactory {

    private final DestinationForwarder forwarder;

    public ResponseProducerFactory(DestinationForwarder forwarder) {
        this.forwarder = forwarder;
    }

    public ResponseProducer forwardingProducer(String destinationUrl) {
        return request -> InboundExchangeResponseResult.forwarded(
                forwarder.forward(request, destinationUrl));
    }

    public ResponseProducer modifiedProducer(String destinationUrl, InboundResponse modifiedResponse) {
        return request -> InboundExchangeResponseResult.modifyForwarded(
                forwarder.forward(request, destinationUrl), modifiedResponse);
    }

}
