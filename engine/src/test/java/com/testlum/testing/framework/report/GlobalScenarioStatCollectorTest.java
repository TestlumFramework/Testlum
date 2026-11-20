package com.testlum.testing.framework.report;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link GlobalScenarioStatCollector} verifying result
 * collection and recalculation.
 */
class GlobalScenarioStatCollectorTest {

    private final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();

    @Test
    void initiallyEmpty() {
        assertTrue(collector.getResults().isEmpty());
    }

    @Test
    void addResultIncreasesSize() {
        final ScenarioResult result = new ScenarioResult();
        result.setName("test-scenario");
        result.setSuccess(true);
        collector.addResult(result);
        assertEquals(1, collector.getResults().size());
        assertEquals("test-scenario", collector.getResults().get(0).getName());
    }

    @Test
    void addMultipleResults() {
        collector.addResult(new ScenarioResult());
        collector.addResult(new ScenarioResult());
        collector.addResult(new ScenarioResult());
        assertEquals(3, collector.getResults().size());
    }

    @Test
    void keepsEveryResultWhenScenariosFinishAtTheSameTime() throws Exception {
        final int threads = 8;
        final int perThread = 5000;
        final java.util.concurrent.ExecutorService pool = java.util.concurrent.Executors.newFixedThreadPool(threads);
        final java.util.concurrent.CountDownLatch start = new java.util.concurrent.CountDownLatch(1);
        final java.util.List<java.util.concurrent.Future<?>> done = new java.util.ArrayList<>();
        for (int t = 0; t < threads; t++) {
            done.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < perThread; i++) {
                    collector.addResult(new ScenarioResult());
                }
                return null;
            }));
        }
        start.countDown();
        for (java.util.concurrent.Future<?> f : done) {
            f.get();
        }
        pool.shutdown();
        assertEquals(threads * perThread, collector.getResults().size());
    }

    @Test
    void recalculateReturnsInfo() {
        final GlobalScenarioStatCollector.ReCalculatedInfo info = collector.recalculate();
        assertNotNull(info);
    }
}
