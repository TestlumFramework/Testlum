package com.knubisoft.testlum.testing.framework.env.service;

import com.knubisoft.testlum.testing.framework.env.EnvManager;
import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class EnvironmentExecutionService {

    private static final int AWAIT_TERMINATION_TIMEOUT_IN_SECONDS = 60;

    private final Map<String, ExecutorService> executors;

    public EnvironmentExecutionService(final List<Environment> environments) {
        this.executors = environments.stream()
                .collect(Collectors.toMap(
                        Environment::getFolder,
                        env -> Executors.newFixedThreadPool(
                                Math.max(1, env.getThreads()),
                                new NamedThreadFactory("env-" + env.getFolder() + "-worker"))));
    }

    public void runInEnvironment(final String environment, final Runnable task) {
        ExecutorService executor = executors.get(environment);
        if (executor == null) {
            throw new DefaultFrameworkException("Environment <%s> is not configured", environment);
        }
        Future<?> future = executor.submit(() -> {
            EnvManager.setCurrentEnv(environment);
            try {
                task.run();
            } finally {
                EnvManager.clearCurrentEnv();
            }
        });
        waitForCompletion(future);
    }

    private void waitForCompletion(final Future<?> future) {
        try {
            future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DefaultFrameworkException(e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            throw new DefaultFrameworkException(cause);
        }
    }

    @PreDestroy
    public void shutdown() {
        executors.forEach(this::shutdownAndAwaitTermination);
    }

    private void shutdownAndAwaitTermination(final String environment, final ExecutorService pool) {
        pool.shutdown();
        try {
            if (!pool.awaitTermination(AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
                pool.shutdownNow();
                if (!pool.awaitTermination(AWAIT_TERMINATION_TIMEOUT_IN_SECONDS, TimeUnit.SECONDS)) {
                    log.error("{} pool did not terminate", environment);
                }
            }
        } catch (final InterruptedException e) {
            pool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    private static final class NamedThreadFactory implements ThreadFactory {
        private final AtomicInteger counter = new AtomicInteger();
        private final String prefix;

        private NamedThreadFactory(final String prefix) {
            this.prefix = prefix;
        }

        @Override
        public Thread newThread(final @NonNull Runnable runnable) {
            Thread thread = new Thread(runnable, prefix + "-" + counter.incrementAndGet());
            thread.setDaemon(false);
            return thread;
        }
    }
}
