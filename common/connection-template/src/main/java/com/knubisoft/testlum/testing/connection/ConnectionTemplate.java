package com.knubisoft.testlum.testing.connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Supplier;

@FunctionalInterface
public interface ConnectionTemplate {

    Logger LOG = LoggerFactory.getLogger(ConnectionTemplate.class);

    int DEFAULT_ATTEMPTS = 3;

    default <T> T executeWithRetry(String integrationName,
                                   Supplier<T> connector,
                                   IntegrationHealthCheck<T> healthCheck) {
        return executeWithRetry(integrationName,
                DEFAULT_ATTEMPTS,
                connector,
                healthCheck, integration -> {
                    if (integration instanceof AutoCloseable closeable) {
                        try {
                            closeable.close();
                        } catch (final Exception e) {
                            LOG.warn("Failed to close integration resource: {}", integrationName, e);
                        }
                    }
                });
    }

    <T> T executeWithRetry(String integrationName,
                           int maxAttempts,
                           Supplier<T> connector,
                           IntegrationHealthCheck<T> healthCheck,
                           IntegrationCloser<T> integrationCloser);
}