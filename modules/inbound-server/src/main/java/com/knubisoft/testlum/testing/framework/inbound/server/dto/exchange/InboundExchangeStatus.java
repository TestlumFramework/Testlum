package com.knubisoft.testlum.testing.framework.inbound.server.dto.exchange;

public enum InboundExchangeStatus {

    RECEIVED, // request has been received
    TIMEOUT, // request has timed out
    BELOW_MIN_RETRIES, // initial request arrived, but min required retries not reached
    UNEXPECTED_EXTRA_RETRY, // unexpected retry has been received
    ERROR // error while processing the request

}