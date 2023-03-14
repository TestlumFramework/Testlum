package com.knubisoft.cott.testing.framework.env.parallel;

import com.knubisoft.cott.testing.framework.configuration.GlobalTestConfigurationProvider;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class EnvironmentsParallelConfiguration implements ParallelExecutionConfigurationStrategy {

    //todo parallelism
    private final int parallelism = GlobalTestConfigurationProvider.getEnabledEnvironments().size();
    private final int keepAliveSeconds = 30;

    //CHECKSTYLE:OFF
    @Override
    public ParallelExecutionConfiguration createConfiguration(final ConfigurationParameters configurationParameters) {
        return new ParallelExecutionConfiguration() {
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
                return keepAliveSeconds;
            }
        };
    }
    //CHECKSTYLE:ON
}
