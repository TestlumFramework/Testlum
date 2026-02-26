package com.knubisoft.testlum.testing.framework.env.parallel;

import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class GlobalParallelExecutionConfigStrategy implements ParallelExecutionConfigurationStrategy {

    private static final int DEFAULT_PARALLELISM = 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    private final GlobalTestConfiguration globalTestConfiguration;
    private final List<Environment> environments;

    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters parameters) {
        boolean isParallel = globalTestConfiguration.isParallelExecution();
        int parallelism = isParallel ? environments.stream()
                .mapToInt(Environment::getThreads).sum() : DEFAULT_PARALLELISM;
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
