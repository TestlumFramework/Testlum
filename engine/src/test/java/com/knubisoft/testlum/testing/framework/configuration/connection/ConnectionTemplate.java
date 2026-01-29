package com.knubisoft.testlum.testing.framework.configuration.connection;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTING_INFO;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTIN_SUCCESS;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_ATTEMPT_FAILED;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_ATTEMPT_RETRYING;
import static com.knubisoft.testlum.testing.framework.constant.LogMessage.CONNECTION_COMPLETELY_FAILED;

@Slf4j
@Component
public class ConnectionTemplate {

    private static final int DEFAULT_ATTEMPTS = 3;
    private static final long INITIAL_BACKOFF_MS = 5000;

    public <T> T executeWithRetry(final String integrationName, final Supplier<T> connector) {
        return executeWithRetry(integrationName, DEFAULT_ATTEMPTS, connector);
    }

    public <T> T executeWithRetry(final String integrationName, final int maxAttempts, final Supplier<T> connector) {
        Exception lastException = null;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                log.info(CONNECTING_INFO, integrationName, attempt, maxAttempts);
                T client = connector.get();
                log.info(CONNECTIN_SUCCESS, integrationName);
                return client;
            } catch (Exception e) {
                lastException = e;
                log.warn(CONNECTION_ATTEMPT_FAILED, attempt, integrationName, e.getCause() == null ? e.getMessage() : e.getCause().getMessage());

                if (attempt < maxAttempts) {
                    log.info(CONNECTION_ATTEMPT_RETRYING, integrationName, INITIAL_BACKOFF_MS);
                    sleep();
                }
            }
        }

        log.error(CONNECTION_COMPLETELY_FAILED, integrationName, Objects.requireNonNull(lastException).getMessage());
        throw new DefaultFrameworkException(String.format("Failed to obtain connection for %s with cause: %s", integrationName, lastException.getMessage()));
    }

    private void sleep() {
        try {
            TimeUnit.MILLISECONDS.sleep(INITIAL_BACKOFF_MS);
        } catch (InterruptedException e) {
            // ignored
        }
    }
}
