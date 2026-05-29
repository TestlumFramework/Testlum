package com.knubisoft.testlum.testing.framework.retry;

import com.knubisoft.testing.framework.producer.ResponseProducer;

/**
 * Represents a single retry attempt. It contains all instructions of what
 * the session has to do when new incoming request arrives.
 */
public class RetryAttempt {

    // If expectedFile is null, the actual won't be validated against it.
    private final String expectedFile;
    private final ResponseProducer producer;

    public RetryAttempt(String expectedFile, ResponseProducer producer) {
        this.expectedFile = expectedFile;
        this.producer = producer;
    }

    public String getExpectedFile() {
        return expectedFile;
    }

    public ResponseProducer getProducer() {
        return producer;
    }
}
