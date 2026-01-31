package com.knubisoft.testlum.testing.framework.configuration.connection;

import com.knubisoft.testlum.testing.framework.configuration.connection.health.IntegrationHealthCheck;

import java.util.function.Supplier;

public interface ConnectionTemplate {

    <T> T executeWithRetry(final String integrationName,
                           final Supplier<T> connector,
                           final IntegrationHealthCheck<T> healthCheck);

    <T> T executeWithRetry(final String integrationName,
                           final int maxAttempts,
                           final Supplier<T> connector,
                           final IntegrationHealthCheck<T> healthCheck);
}
