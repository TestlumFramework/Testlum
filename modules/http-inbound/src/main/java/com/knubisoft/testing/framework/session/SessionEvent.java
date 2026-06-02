package com.knubisoft.testing.framework.session;

import com.knubisoft.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeOutput;
import com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange.InboundExchangeStatus;

public interface SessionEvent {

    record Received(int index, InboundRequest request, InboundExchangeOutput output) implements SessionEvent {}

    record Completed() implements SessionEvent {}

    record Failed(InboundExchangeStatus status, String errorMessage, InboundRequest extraIfPresent) implements SessionEvent {}

}
