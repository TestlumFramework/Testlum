package com.knubisoft.testlum.testing.connection;

import com.knubisoft.testlum.log.LogFormat;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

@Slf4j
@Component
public class ConnectionTemplateImpl implements ConnectionTemplate {

    private static final String CONNECTING_INFO =
            LogFormat.withCyan("Connecting to {} (Attempt {}/{})");
    private static final String CONNECTION_SUCCESS =
            LogFormat.withGreen("Successfully connected to {}");
    private static final String CONNECTION_ATTEMPT_FAILED =
            LogFormat.withOrange("Attempt {} failed for {} with error: {}");
    private static final String CONNECTION_ATTEMPT_RETRYING =
            "Retrying to connect to {} in {} ms";
    private static final String CONNECTION_COMPLETELY_FAILED =
            LogFormat.withRed("Max attempts reached for {}. Failed to obtain connection with cause {}");

    private static final long INITIAL_BACKOFF_MS = 20000;

    public <T> T executeWithRetry(final String integrationName,
                                  final int maxAttempts,
                                  final Supplier<T> connector,
                                  final IntegrationHealthCheck<T> healthCheck,
                                  final IntegrationCloser<T> integrationCloser) {

        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return tryConnect(integrationName, attempt, maxAttempts, connector, healthCheck, integrationCloser);
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
                             final IntegrationHealthCheck<T> healthCheck,
                             final IntegrationCloser<T> integrationCloser) throws Exception {
        log.info(CONNECTING_INFO, integrationName, attempt, max);
        T client = connector.get();
        try {
            healthCheck.verify(client);
            log.info(CONNECTION_SUCCESS, integrationName);
            return client;
        } catch (final Exception e) {
            integrationCloser.close(client);
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
        String error = String.format("Failed to obtain connection for %s with cause: %s", integrationName, msg);
        return new IntegrationFailureException(error);
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(INITIAL_BACKOFF_MS);
        } catch (InterruptedException e) {
            // ignored
        }
    }
}
