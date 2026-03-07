package com.knubisoft.testlum.testing.framework.env.parallel;

import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class GlobalParallelExecutionConfigStrategy implements ParallelExecutionConfigurationStrategy {

    private static final int DEFAULT_PARALLELISM = 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters parameters) {
        int parallelism = parameters
                .get("testlum.parallelism")
                .map(Integer::parseInt)
                .orElse(DEFAULT_PARALLELISM);
        return new GlobalParallelExecutionConfiguration(parallelism);
    }

    private record GlobalParallelExecutionConfiguration(int parallelism) implements ParallelExecutionConfiguration {

        @Override
        public int getParallelism() {
            return parallelism;
        }

        @Override
        public int getMinimumRunnable() {
            return Math.max(1, parallelism / 2);
        }

        @Override
        public int getMaxPoolSize() {
            return parallelism * 2;
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
