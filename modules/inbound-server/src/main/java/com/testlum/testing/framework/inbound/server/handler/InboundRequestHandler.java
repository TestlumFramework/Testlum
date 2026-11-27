package com.testlum.testing.framework.inbound.server.handler;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.inbound.server.dto.InboundResponse;

/**
 * A single point of connection between @{link InboundServer} and rest of the system.
 *
 * Any implementation of the server converts raw HTTP request into @{link InboundRequest}
 * and writes it back.
 */
@FunctionalInterface
public interface InboundRequestHandler {

    /**
     * Handles the inbound request and returns the response.
     *
     * This method should not throw any exceptions. Any exception must be
     * converted into @{link InboundResponse}. Otherwise, the connection
     * between server and client will be stuck.
     */
    InboundResponse handle(InboundRequest request);

}
