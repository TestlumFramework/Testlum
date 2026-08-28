package com.testlum.testing.connection;

import com.testlum.testing.framework.exception.DriverCreationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class ConnectionTemplateImplTest {

    private ConnectionTemplateImpl template;

    @BeforeEach
    void setUp() {
        template = new ConnectionTemplateImpl() {
            @Override
            void sleep() {
                // no-op to avoid delays in tests
            }
        };
    }

    @Nested
    class ExecuteWithRetry {
        @Test
        void returnsClientOnFirstSuccessfulAttempt() {
            String result = template.executeWithRetry(
                    "testDB",
                    1,
                    () -> "connected",
                    client -> { },
                    client -> { });

            assertEquals("connected", result);
        }

        @Test
        void retriesOnFailureAndSucceedsOnSecondAttempt() {
            AtomicInteger attempt = new AtomicInteger(0);

            String result = template.executeWithRetry(
                    "testDB",
                    3,
                    () -> "client",
                    client -> {
                        if (attempt.getAndIncrement() == 0) {
                            throw new RuntimeException(
                                    "health check failed");
                        }
                    },
                    client -> { });

            assertEquals("client", result);
            assertEquals(2, attempt.get());
        }

        @Test
        void throwsAfterMaxAttempts() {
            IntegrationFailureException exception = assertThrows(
                    IntegrationFailureException.class,
                    () -> template.executeWithRetry(
                            "failDB",
                            1,
                            () -> "client",
                            client -> {
                                throw new RuntimeException("always fails");
                            },
                            client -> { }));

            assertTrue(exception.getMessage().contains("failDB"));
            assertTrue(exception.getMessage().contains("always fails"));
        }

        @Test
        void closesClientWhenHealthCheckFails() {
            AtomicInteger closeCalls = new AtomicInteger(0);

            assertThrows(IntegrationFailureException.class,
                    () -> template.executeWithRetry(
                            "closableDB",
                            1,
                            () -> "client",
                            client -> {
                                throw new RuntimeException("unhealthy");
                            },
                            client -> closeCalls.incrementAndGet()));

            assertEquals(1, closeCalls.get());
        }

        @Test
        void closesClientOnEachFailedAttempt() {
            AtomicInteger closeCalls = new AtomicInteger(0);

            assertThrows(IntegrationFailureException.class,
                    () -> template.executeWithRetry(
                            "multiCloseDB",
                            2,
                            () -> "client",
                            client -> {
                                throw new RuntimeException("unhealthy");
                            },
                            client -> closeCalls.incrementAndGet()));

            assertEquals(2, closeCalls.get());
        }

        @Test
        void doesNotCloseOnSuccessfulHealthCheck() {
            AtomicInteger closeCalls = new AtomicInteger(0);

            template.executeWithRetry(
                    "healthyDB",
                    1,
                    () -> "client",
                    client -> { },
                    client -> closeCalls.incrementAndGet());

            assertEquals(0, closeCalls.get());
        }

        @Test
        void returnsResultFromConnectorSupplier() {
            Object customClient = new Object();

            Object result = template.executeWithRetry(
                    "customDB",
                    1,
                    () -> customClient,
                    client -> { },
                    client -> { });

            assertSame(customClient, result);
        }

        @Test
        void throwsWhenConnectorFails() {
            IntegrationFailureException exception = assertThrows(
                    IntegrationFailureException.class,
                    () -> template.executeWithRetry(
                            "brokenDB",
                            1,
                            () -> {
                                throw new RuntimeException(
                                        "connection refused");
                            },
                            client -> { },
                            client -> { }));

            assertTrue(exception.getMessage().contains("brokenDB"));
        }
    }

    @Nested
    class FormattedFailureHandling {
        @Test
        void stopsRetryingWhenFailureIsNotRetryable() {
            AtomicInteger attempts = new AtomicInteger(0);

            DriverCreationException exception = assertThrows(
                    DriverCreationException.class,
                    () -> template.executeWithRetry(
                            "chrome",
                            3,
                            () -> {
                                attempts.incrementAndGet();
                                throw new DriverCreationException("bad credentials", false, null);
                            },
                            client -> {
                            },
                            client -> {
                            }));

            assertEquals(1, attempts.get());
            assertEquals("bad credentials", exception.describe());
        }

        @Test
        void keepsRetryingWhenFailureIsRetryable() {
            AtomicInteger attempts = new AtomicInteger(0);

            assertThrows(DriverCreationException.class,
                    () -> template.executeWithRetry(
                            "chrome",
                            3,
                            () -> {
                                attempts.incrementAndGet();
                                throw new DriverCreationException("server is down", true, null);
                            },
                            client -> {
                            },
                            client -> {
                            }));

            assertEquals(3, attempts.get());
        }

        @Test
        void rethrowsFormattedFailureWithoutWrappingIt() {
            DriverCreationException original = new DriverCreationException("formatted block", false, null);

            RuntimeException thrown = assertThrows(
                    RuntimeException.class,
                    () -> template.executeWithRetry(
                            "chrome",
                            1,
                            () -> {
                                throw original;
                            },
                            client -> {
                            },
                            client -> {
                            }));

            assertSame(original, thrown);
        }

        @Test
        void keepsCauseWhenWrappingPlainFailure() {
            RuntimeException cause = new RuntimeException("boom");

            IntegrationFailureException exception = assertThrows(
                    IntegrationFailureException.class,
                    () -> template.executeWithRetry(
                            "redis",
                            1,
                            () -> {
                                throw cause;
                            },
                            client -> {
                            },
                            client -> {
                            }));

            assertSame(cause, exception.getCause());
        }
    }

    @Nested
    class DefaultRetryBehavior {
        @Test
        void defaultAttemptsConstantIsThree() {
            assertEquals(3, ConnectionTemplate.DEFAULT_ATTEMPTS);
        }

        @Test
        void succeedsWithDefaultRetryMethod() {
            String result = template.executeWithRetry(
                    "test",
                    () -> "ok",
                    client -> { });

            assertEquals("ok", result);
        }
    }
}
