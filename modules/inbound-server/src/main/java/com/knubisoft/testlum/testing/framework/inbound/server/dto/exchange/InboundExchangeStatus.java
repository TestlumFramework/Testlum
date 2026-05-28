package com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange;

public enum InboundExchangeStatus {

    RECEIVED, // request has been received
    TIMEOUT, // request has timed out
    ERROR // error while processing the request

}