package com.knubisoft.testlum.testing.framework.configuration.connection;

import com.knubisoft.testlum.testing.framework.configuration.connection.health.IntegrationHealthCheck;

import java.util.function.Supplier;

public interface ConnectionTemplate {

    <T> T executeWithRetry(String integrationName,
                           Supplier<T> connector,
                           IntegrationHealthCheck<T> healthCheck);

    <T> T executeWithRetry(String integrationName,
                           int maxAttempts,
                           Supplier<T> connector,
                           IntegrationHealthCheck<T> healthCheck);
}
