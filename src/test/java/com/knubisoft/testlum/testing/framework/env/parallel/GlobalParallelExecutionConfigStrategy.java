package com.knubisoft.testlum.testing.framework.env.parallel;

import com.knubisoft.testlum.testing.framework.configuration.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.parser.XMLParsers;
import com.knubisoft.testlum.testing.framework.validator.GlobalTestConfigValidator;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import lombok.RequiredArgsConstructor;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class GlobalParallelExecutionConfigStrategy implements ParallelExecutionConfigurationStrategy {

    private static final int DEFAULT_PARALLELISM = 1;
    private static final int KEEP_ALIVE_SECONDS = 30;

    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters parameters) {
        GlobalTestConfiguration globalTestConfiguration = XMLParsers.forGlobalTestConfiguration()
                .process(TestResourceSettings.getInstance().getConfigFile(), new GlobalTestConfigValidator());
        boolean isParallelExecutionEnabled = globalTestConfiguration.isParallelExecution();
        int parallelism = isParallelExecutionEnabled
                ? globalTestConfiguration.getEnvironments().getEnv().stream()
                .filter(Environment::isEnabled)
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
