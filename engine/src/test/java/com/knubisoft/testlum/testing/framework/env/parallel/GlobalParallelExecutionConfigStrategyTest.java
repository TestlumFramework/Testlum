package com.knubisoft.testlum.testing.framework.env.parallel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.ConfigurationParameters;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalParallelExecutionConfigStrategyTest {

    private GlobalParallelExecutionConfigStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new GlobalParallelExecutionConfigStrategy();
    }

    @Test
    void createConfigurationWithParallelismFour() {
        ConfigurationParameters parameters = mock(ConfigurationParameters.class);
        when(parameters.get("testlum.parallelism")).thenReturn(Optional.of("4"));

        var config = strategy.createConfiguration(parameters);

        assertEquals(4, config.getParallelism());
        assertEquals(2, config.getMinimumRunnable());
        assertEquals(8, config.getMaxPoolSize());
        assertEquals(4, config.getCorePoolSize());
        assertEquals(30, config.getKeepAliveSeconds());
    }

    @Test
    void createConfigurationWithNoParameterDefaultsToOne() {
        ConfigurationParameters parameters = mock(ConfigurationParameters.class);
        when(parameters.get("testlum.parallelism")).thenReturn(Optional.empty());

        var config = strategy.createConfiguration(parameters);

        assertEquals(1, config.getParallelism());
        assertEquals(1, config.getMinimumRunnable());
        assertEquals(2, config.getMaxPoolSize());
        assertEquals(1, config.getCorePoolSize());
        assertEquals(30, config.getKeepAliveSeconds());
    }

    @Test
    void createConfigurationWithParallelismOne() {
        ConfigurationParameters parameters = mock(ConfigurationParameters.class);
        when(parameters.get("testlum.parallelism")).thenReturn(Optional.of("1"));

        var config = strategy.createConfiguration(parameters);

        assertEquals(1, config.getParallelism());
        assertEquals(1, config.getMinimumRunnable());
        assertEquals(2, config.getMaxPoolSize());
        assertEquals(1, config.getCorePoolSize());
        assertEquals(30, config.getKeepAliveSeconds());
    }
}
