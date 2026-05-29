package com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange;

public abstract class AbstractInboundExchange {

    private final InboundExchangeStatus status;
    private final String errorMessage;

    protected AbstractInboundExchange(InboundExchangeStatus status, String errorMessage) {
        this.status = status;
        this.errorMessage = errorMessage;
    }

    public InboundExchangeStatus getStatus() {
        return status;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public boolean isReceived() {
        return status == InboundExchangeStatus.RECEIVED;
    }
}
