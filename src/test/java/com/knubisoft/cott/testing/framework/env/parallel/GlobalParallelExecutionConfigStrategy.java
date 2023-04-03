package com.knubisoft.cott.testing.framework.env.parallel;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import com.knubisoft.cott.testing.model.global_config.Environment;
import lombok.RequiredArgsConstructor;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class GlobalParallelExecutionConfigStrategy implements ParallelExecutionConfigurationStrategy {

    private static final int DEFAULT_PARALLELISM = 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters parameters) {
        boolean isParallelExecutionEnabled = GlobalTestConfigurationProvider.provide().isParallelExecution();
        int parallelism = isParallelExecutionEnabled
                ? GlobalTestConfigurationProvider.getEnabledEnvironments().stream()
                .mapToInt(Environment::getThreads).sum()
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
