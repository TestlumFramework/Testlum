package com.knubisoft.cott.testing;

import com.knubisoft.cott.testing.framework.util.BrowserUtil;
import org.junit.platform.engine.ConfigurationParameters;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfiguration;
import org.junit.platform.engine.support.hierarchical.ParallelExecutionConfigurationStrategy;

public class CustomStrategy implements ParallelExecutionConfigurationStrategy {
    @Override
    public ParallelExecutionConfiguration createConfiguration(ConfigurationParameters configurationParameters) {
        int parallelism = BrowserUtil.filterEnabledBrowsers().size();
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
                return 30;
            }
        };
    }
}
