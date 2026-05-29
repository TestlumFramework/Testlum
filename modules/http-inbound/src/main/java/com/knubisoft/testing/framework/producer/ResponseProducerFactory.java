package com.knubisoft.testing.framework.producer;

import com.knubisoft.testing.framework.forwarder.DestinationForwarder;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import org.springframework.stereotype.Component;

@Component
public class ResponseProducerFactory {

    private final DestinationForwarder forwarder;

    public ResponseProducerFactory(DestinationForwarder forwarder) {
        this.forwarder = forwarder;
    }

    public ResponseProducer forwardingProducer(String destinationUrl) {
        return request -> InboundExchangeOutput.forwarded(
                forwarder.forward(request, destinationUrl));
    }

    public ResponseProducer modifiedProducer(String destinationUrl, InboundResponse modifiedResponse) {
        return request -> InboundExchangeOutput.modifyForwarded(
                forwarder.forward(request, destinationUrl), modifiedResponse);
    }

}
