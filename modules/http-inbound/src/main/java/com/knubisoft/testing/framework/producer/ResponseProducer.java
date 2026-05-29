package com.knubisoft.testing.framework.producer;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;

/**
 * Produces a response from the destination for an incoming request.
 * It gets built by the interpretator but gets executed by the handler during HTTP exchange.
 * <p>
 * Forwarding the webhook request to the destination has to be done in a synchronous manner
 * during exchange, so we can send the response back to the sender. So, this producer
 * presents a chosen strategy of how to produce the response and gets called by the handler
 * whenever needed.
 * <p>
 * Implementations:
 * - mock-producer: Produces a mock response {@code #ResponseOutcome.mock}
 * - forward-producer: Sends the request to the destination and
 *  returns the real response {@code #ResponseOutcome.forwarded}
 */
@FunctionalInterface
public interface ResponseProducer {

    InboundExchangeOutput produce(InboundRequest request) throws Exception;

}
