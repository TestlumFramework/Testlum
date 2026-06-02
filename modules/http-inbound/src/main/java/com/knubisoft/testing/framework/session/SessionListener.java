package com.knubisoft.testing.framework.session;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;

/**
 * Double-channel from {@link CapturingSession} to interpreter. The session
 * calls these methods on a new event arrival (not after the completion
 * of the session), so interpreter can react to the event in runtime.
 */
public interface SessionListener {

    /** Called when a new request is received. */
    void onRequestReceived(int attemptIndex, InboundRequest request, InboundExchangeOutput output);

    /** Called when the session is completed. */
    void onComplete();

    /** Called when the session failed. */
    void onFailure(InboundExchangeStatus status, String errorMessage, InboundRequest extraIfPresent);

}
