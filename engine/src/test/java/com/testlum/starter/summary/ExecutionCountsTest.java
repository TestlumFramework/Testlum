package com.testlum.starter.summary;

import com.testlum.testing.framework.scenario.ScenarioStatusRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.File;
import java.util.EnumMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExecutionCountsTest {

    @BeforeEach
    void clearRegistry() {
        ScenarioStatusRegistry.clear();
    }

    @AfterEach
    void cleanUp() {
        ScenarioStatusRegistry.clear();
    }

    @Nested
    class Found {

        @Test
        void isTheSumOfEveryOtherCounter() {
            assertEquals(30, new ExecutionCounts(2, 3, 20, 5).found());
        }

        @Test
        void isZeroWhenNothingWasAccountedFor() {
            assertEquals(0, new ExecutionCounts(0, 0, 0, 0).found());
        }
    }

    @Nested
    class Of {

        @Test
        void readsRunsFromSummaryAndScenariosFromRegistry() {
            ScenarioStatusRegistry.registerInvalid(new File("broken.xml"), "xsd");
            ScenarioStatusRegistry.registerSkipped(new File("off.xml"), "inactive");
            ScenarioStatusRegistry.registerSkipped(new File("tagged.xml"), "tags");

            ExecutionCounts counts = ExecutionCounts.of(summary(8, 2, 0));

            assertEquals(1, counts.invalid());
            assertEquals(2, counts.skipped());
            assertEquals(8, counts.successful());
            assertEquals(2, counts.failed());
            assertEquals(13, counts.found());
        }

        @Test
        void foldsAbortedRunsIntoFailed() {
            ExecutionCounts counts = ExecutionCounts.of(summary(1, 2, 3));

            assertEquals(5, counts.failed());
        }

        private TestExecutionSummary summary(final long succeeded, final long failed, final long aborted) {
            TestExecutionSummary summary = mock(TestExecutionSummary.class);
            when(summary.getTestsSucceededCount()).thenReturn(succeeded);
            when(summary.getTestsFailedCount()).thenReturn(failed);
            when(summary.getTestsAbortedCount()).thenReturn(aborted);
            return summary;
        }
    }

    @Nested
    class Rows {

        @Test
        void everyRowReadsItsOwnCounter() {
            ExecutionCounts counts = new ExecutionCounts(2, 3, 20, 5);
            Map<TestExecutionResult, Long> byRow = new EnumMap<>(TestExecutionResult.class);
            for (TestExecutionResult result : TestExecutionResult.values()) {
                byRow.put(result, result.countIn(counts));
            }

            assertEquals(30, byRow.get(TestExecutionResult.FOUND));
            assertEquals(2, byRow.get(TestExecutionResult.INVALID));
            assertEquals(3, byRow.get(TestExecutionResult.SKIPPED));
            assertEquals(20, byRow.get(TestExecutionResult.SUCCESSFUL));
            assertEquals(5, byRow.get(TestExecutionResult.FAILED));
        }

        @Test
        void foundRowEqualsTheSumOfTheOthers() {
            ExecutionCounts counts = new ExecutionCounts(1, 4, 9, 6);
            long sum = TestExecutionResult.INVALID.countIn(counts)
                    + TestExecutionResult.SKIPPED.countIn(counts)
                    + TestExecutionResult.SUCCESSFUL.countIn(counts)
                    + TestExecutionResult.FAILED.countIn(counts);

            assertEquals(sum, TestExecutionResult.FOUND.countIn(counts));
        }
    }
}
