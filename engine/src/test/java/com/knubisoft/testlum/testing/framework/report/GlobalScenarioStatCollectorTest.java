package com.knubisoft.testlum.testing.framework.report;

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
    void recalculateReturnsInfo() {
        final GlobalScenarioStatCollector.ReCalculatedInfo info = collector.recalculate();
        assertNotNull(info);
    }
}
