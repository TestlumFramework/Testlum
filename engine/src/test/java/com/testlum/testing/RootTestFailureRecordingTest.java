package com.testlum.testing;

import com.testlum.testing.framework.report.GlobalScenarioStatCollector;
import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.scenario.ScenarioArguments;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RootTestFailureRecordingTest {

    @Test
    void aScenarioThatThrewOutsideItsStepsIsAddedToTheCollectorAsFailed() {
        GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
        ScenarioArguments args = ScenarioArguments.builder().file(new File("scenarios/s1/scenario.xml")).build();

        RootTest.recordFailureOutsideStep(collector, args, new IllegalArgumentException("Unable to find value"));

        assertEquals(1, collector.getResults().size());
        ScenarioResult result = collector.getResults().get(0);
        assertFalse(result.isSuccess());
        assertEquals(new File("scenarios/s1/scenario.xml").getPath(), result.getPath());
        assertEquals("before step 1", result.getFailedStep().label());
    }

    @Test
    void aRunnerThatThrowsRecordsTheFailureOnceAndRethrowsTheSameException() {
        GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
        ScenarioArguments args = ScenarioArguments.builder().file(new File("scenarios/s1/scenario.xml")).build();
        IllegalStateException boom = new IllegalStateException("boom");

        IllegalStateException thrown = assertThrows(IllegalStateException.class,
                () -> RootTest.runRecordingFailureOutsideStep(collector, args, () -> {
                    throw boom;
                }));

        assertSame(boom, thrown);
        assertEquals(1, collector.getResults().size());
        assertFalse(collector.getResults().get(0).isSuccess());
        assertEquals("before step 1", collector.getResults().get(0).getFailedStep().label());
    }

    @Test
    void aRunnerThatReturnsAddsNothingAndPassesTheResultThrough() {
        GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
        ScenarioArguments args = ScenarioArguments.builder().file(new File("scenarios/s1/scenario.xml")).build();
        ScenarioResult expected = ScenarioResult.failedOutsideStep("elsewhere", new RuntimeException("x"));

        ScenarioResult actual = RootTest.runRecordingFailureOutsideStep(collector, args, () -> expected);

        assertSame(expected, actual);
        assertEquals(0, collector.getResults().size());
    }
}
