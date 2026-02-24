package com.knubisoft.testlum.testing.connection;

import java.util.function.Supplier;

@FunctionalInterface
public interface ConnectionTemplate {

    int DEFAULT_ATTEMPTS = 3;

    default <T> T executeWithRetry(String integrationName,
                                   Supplier<T> connector,
                                   IntegrationHealthCheck<T> healthCheck) {
        return executeWithRetry(integrationName,
                DEFAULT_ATTEMPTS,
                connector,
                healthCheck, integration -> {
                    if (integration instanceof AutoCloseable) {
                        try {
                            ((AutoCloseable) integration).close();
                        } catch (final Exception ignored) {
                            // ignore
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