package com.knubisoft.testlum.starter;

import com.knubisoft.testlum.starter.summary.TestExecutionPostProcessor;
import com.knubisoft.testlum.testing.framework.TestResourceSettings;
import com.knubisoft.testlum.testing.framework.xml.XMLParsers;
import com.knubisoft.testlum.testing.model.global_config.Environment;
import com.knubisoft.testlum.testing.model.global_config.Environments;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TestRunnerTest {

    @Mock
    private XMLParsers xmlParsers;
    @Mock
    private TestResourceSettings testResourceSettings;
    @Mock
    private TestExecutionPostProcessor testExecutionPostProcessor;

    private TestRunner testRunner;

    @BeforeEach
    void setUp() {
        testRunner = new TestRunner(xmlParsers, testResourceSettings, testExecutionPostProcessor);
    }

    @Nested
    class IsParallel {

        @Test
        void returnsTrueWhenParallelEnabled() throws Exception {
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            config.setParallelExecution(true);

            Method method = TestRunner.class.getDeclaredMethod("isParallel", GlobalTestConfiguration.class);
            method.setAccessible(true);

            assertTrue((boolean) method.invoke(testRunner, config));
        }

        @Test
        void returnsFalseWhenParallelDisabled() throws Exception {
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            config.setParallelExecution(false);

            Method method = TestRunner.class.getDeclaredMethod("isParallel", GlobalTestConfiguration.class);
            method.setAccessible(true);

            assertFalse((boolean) method.invoke(testRunner, config));
        }

        @Test
        void returnsFalseWhenParallelIsNull() throws Exception {
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            config.setParallelExecution(null);

            Method method = TestRunner.class.getDeclaredMethod("isParallel", GlobalTestConfiguration.class);
            method.setAccessible(true);

            assertFalse((boolean) method.invoke(testRunner, config));
        }
    }

    @Nested
    class ComputeParallelism {

        @Test
        void returnsAtLeastOne() throws Exception {
            GlobalTestConfiguration config = createConfigWithThreads(0);

            Method method = TestRunner.class.getDeclaredMethod("computeParallelism", GlobalTestConfiguration.class);
            method.setAccessible(true);
            int result = (int) method.invoke(testRunner, config);

            assertTrue(result >= 1);
        }

        @Test
        void doesNotExceedTwiceCpuCount() throws Exception {
            GlobalTestConfiguration config = createConfigWithThreads(10000);

            Method method = TestRunner.class.getDeclaredMethod("computeParallelism", GlobalTestConfiguration.class);
            method.setAccessible(true);
            int result = (int) method.invoke(testRunner, config);

            int maxExpected = Runtime.getRuntime().availableProcessors() * 2;
            assertTrue(result <= maxExpected);
        }

        @Test
        void sumsThreadsFromMultipleEnvironments() throws Exception {
            GlobalTestConfiguration config = createConfigWithMultipleEnvThreads(4, 6);

            Method method = TestRunner.class.getDeclaredMethod("computeParallelism", GlobalTestConfiguration.class);
            method.setAccessible(true);
            int result = (int) method.invoke(testRunner, config);

            int cpuLimit = Runtime.getRuntime().availableProcessors() * 2;
            int expected = Math.max(1, Math.min(10, cpuLimit));
            assertEquals(expected, result);
        }

        private GlobalTestConfiguration createConfigWithThreads(final int threads) {
            Environment env = new Environment();
            env.setThreads(threads);
            Environments environments = new Environments();
            environments.getEnv().add(env);
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            config.setEnvironments(environments);
            return config;
        }

        private GlobalTestConfiguration createConfigWithMultipleEnvThreads(final int... threadCounts) {
            List<Environment> envList = java.util.Arrays.stream(threadCounts)
                    .mapToObj(t -> {
                        Environment env = new Environment();
                        env.setThreads(t);
                        return env;
                    })
                    .toList();
            Environments environments = new Environments();
            environments.getEnv().addAll(envList);
            GlobalTestConfiguration config = new GlobalTestConfiguration();
            config.setEnvironments(environments);
            return config;
        }
    }
}
