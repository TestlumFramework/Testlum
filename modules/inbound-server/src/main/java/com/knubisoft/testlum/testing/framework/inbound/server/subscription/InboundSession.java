package com.knubisoft.testlum.testing.framework.inbound.server.subscription;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundResponse;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchange;

/**
 * Session associated with a single inbound test step.
 * It lives until the test step is finished.
 * <p>
 * Usage looks like this:
 * - testing thread creates this session and calls {@link #awaitExchange()} method;
 * - from the server side, handlers match correlation ID and call {@link #onRequest}
 *  for each incoming request; that's why {@link #onRequest} has to be thread-safe.
 */
public interface InboundSession {

    /**
     * Endpoint on which this session is expecting requests (from integration).
     */
    String endpoint();

    /**
     * Handles the inbound request for this session.
     */
    InboundResponse onRequest(InboundRequest request);

    /**
     * Caller-thread waits until the session is completed.
     */
    InboundExchange awaitExchange();

    /**
     * To check whether the session is completed and could be remove from the session registry
     */
    boolean isCompleted();

}
