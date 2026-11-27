package com.testlum.starter.summary;

import com.testlum.testing.framework.scenario.ScenarioStatusRegistry;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

/**
 * Counters behind the final result table.
 *
 * @param invalid    scenarios that were expected to run but could not
 * @param skipped    scenarios configured not to run
 * @param successful runs that finished successfully
 * @param failed     runs that finished with an error
 */
public record ExecutionCounts(long invalid, long skipped, long successful, long failed) {

    public static ExecutionCounts of(final TestExecutionSummary summary) {
        return new ExecutionCounts(
                ScenarioStatusRegistry.getInvalid().size(),
                ScenarioStatusRegistry.getSkipped().size(),
                summary.getTestsSucceededCount(),
                summary.getTestsFailedCount() + summary.getTestsAbortedCount());
    }

    public long found() {
        return this.invalid + this.skipped + this.successful + this.failed;
    }
}
