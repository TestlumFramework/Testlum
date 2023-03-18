package com.knubisoft.cott.testing.framework.env.parallel;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import lombok.RequiredArgsConstructor;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class GlobalParallelExecutionConfigStrategy implements ParallelExecutionConfigurationStrategy {

    private static final int DEFAULT_PARALLELISM = 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    private final boolean isEnabled = GlobalTestConfigurationProvider.provide().getParallelExecution().isEnabled();
    private final int envCount = GlobalTestConfigurationProvider.getEnabledEnvironments().size();
    private final int threadCount = GlobalTestConfigurationProvider.provide().getParallelExecution().getThreads();

    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters parameters) {
        int parallelism = isEnabled
                ? Math.multiplyExact(envCount, Math.max(threadCount, DEFAULT_PARALLELISM))
                : DEFAULT_PARALLELISM;
        return new GlobalParallelExecutionConfiguration(parallelism);
    }

    @RequiredArgsConstructor
    private static class GlobalParallelExecutionConfiguration implements ParallelExecutionConfiguration {
        private final int parallelism;

        @Override
        public int getParallelism() {
            return parallelism;
        }

        @Override
        public int getMinimumRunnable() {
            return parallelism;
        }

        @Override
        public int getMaxPoolSize() {
            return parallelism;
        }

        @Override
        public int getCorePoolSize() {
            return parallelism;
        }

        @Override
        public int getKeepAliveSeconds() {
            return KEEP_ALIVE_SECONDS;
        }
    }
}
