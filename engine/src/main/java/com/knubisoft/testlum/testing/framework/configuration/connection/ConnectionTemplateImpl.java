package com.knubisoft.testlum.testing.framework.configuration.connection;

import com.knubisoft.testlum.testing.framework.configuration.connection.health.IntegrationHealthCheck;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.WebDriver;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.*;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_ATTEMPT_RETRYING;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_COMPLETELY_FAILED;

@Slf4j
@Component
public class ConnectionTemplateImpl implements ConnectionTemplate {

    private static final int DEFAULT_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 20000;

    public <T> T executeWithRetry(final String integrationName,
                                  final Supplier<T> connector,
                                  final IntegrationHealthCheck<T> healthCheck) {
        return executeWithRetry(integrationName, DEFAULT_ATTEMPTS, connector, healthCheck);
    }

    public <T> T executeWithRetry(final String integrationName,
                                  final int maxAttempts,
                                  final Supplier<T> connector,
                                  final IntegrationHealthCheck<T> healthCheck) {

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return tryConnect(integrationName, attempt, maxAttempts, connector, healthCheck);
            } catch (Exception e) {
                lastException = e;
                handleAttemptFailure(integrationName, attempt, maxAttempts, e);
            }
        }

        throw handleFinalFailure(integrationName, lastException);
    }

    private <T> T tryConnect(final String integrationName,
                             final int attempt,
                             final int max,
                             final Supplier<T> connector,
                             final IntegrationHealthCheck<T> healthCheck) throws Exception {
        log.info(CONNECTING_INFO, integrationName, attempt, max);
        T client = connector.get();
        try {
            healthCheck.verify(client);
            log.info(CONNECTIN_SUCCESS, integrationName);
            return client;
        } catch (final Exception e) {
            closeResource(client);
            throw e;
        }
    }

    private void handleAttemptFailure(final String integrationName, final int attempt,
                                      final int max, final Exception e) {
        log.warn(CONNECTION_ATTEMPT_FAILED, attempt, integrationName, e.getMessage());
        if (attempt < max) {
            log.info(CONNECTION_ATTEMPT_RETRYING, integrationName, INITIAL_BACKOFF_MS);
            sleep();
        }
    }

    private RuntimeException handleFinalFailure(final String integrationName,
                                                final Exception lastException) {
        String msg = lastException != null ? lastException.getMessage() : "Unknown error";
        log.error(CONNECTION_COMPLETELY_FAILED, integrationName, msg);
        return new DefaultFrameworkException(
                String.format("Failed to obtain connection for %s with cause: %s", integrationName, msg)
        );
    }

    private void closeResource(final Object resource) {
        if (resource instanceof WebDriver) {
            try {
                ((WebDriver) resource).quit();
            } catch (final Exception e) {
                throw new DefaultFrameworkException("Failed to quit WebDriver: ".concat(e.getMessage()));
            }
        }
        if (resource instanceof AutoCloseable) {
            try {
                ((AutoCloseable) resource).close();
            } catch (final Exception ignored) {
                // ignore
            }
        }
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(INITIAL_BACKOFF_MS);
        } catch (InterruptedException e) {
            // ignored
        }
    }
}
