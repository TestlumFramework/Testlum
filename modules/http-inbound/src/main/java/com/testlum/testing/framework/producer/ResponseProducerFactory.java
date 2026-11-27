package com.testlum.testing.framework.producer;

import com.testlum.testing.framework.forwarder.DestinationForwarder;
import com.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;

/**
 * Created as a bean in {@code InboundServerConfiguration}. Not a {@code @Component}: the package is
 * component-scanned, so it would be registered twice.
 */
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
