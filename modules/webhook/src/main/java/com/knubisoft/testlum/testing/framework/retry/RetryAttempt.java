package com.knubisoft.testlum.testing.framework.retry;

import com.knubisoft.testing.framework.producer.ResponseProducer;

/**
 * Represents a single retry attempt. It contains all instructions of what
 * the session has to do when new incoming request arrives.
 */
public class RetryAttempt {

    // If expectedFile is null, the actual won't be validated against it.
    private final String expectedRequestFile;
    private final Object expectedDestinationResponse;
    private final ResponseProducer producer;

    public RetryAttempt(String expectedRequestFile, ResponseProducer producer) {
        this(expectedRequestFile, null, producer);
    }

    public RetryAttempt(String expectedRequestFile, Object expectedDestinationResponse, ResponseProducer producer) {
        this.expectedRequestFile = expectedRequestFile;
        this.expectedDestinationResponse = expectedDestinationResponse;
        this.producer = producer;
    }

    public String getExpectedRequestFile() {
        return expectedRequestFile;
    }

    public Object getExpectedDestinationResponse() {
        return expectedDestinationResponse;
    }

    public ResponseProducer getProducer() {
        return producer;
    }
}
