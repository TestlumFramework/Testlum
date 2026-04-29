package com.knubisoft.testlum.starter.summary;

import com.knubisoft.testlum.log.Color;
import lombok.RequiredArgsConstructor;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.util.function.ToLongFunction;

@RequiredArgsConstructor
public enum TestExecutionResult {

    FOUND("Found", TestExecutionSummary::getTestsFoundCount, Color.CYAN),
    STARTED("Started", TestExecutionSummary::getTestsStartedCount, Color.CYAN),
    SUCCEEDED("Successful", TestExecutionSummary::getTestsSucceededCount, Color.GREEN),
    FAILED("Failed", TestExecutionSummary::getTestsFailedCount, Color.RED),
    SKIPPED("Skipped", TestExecutionSummary::getTestsSkippedCount, Color.ORANGE),
    ABORTED("Aborted", TestExecutionSummary::getTestsAbortedCount, Color.YELLOW);

    private final String status;
    private final ToLongFunction<TestExecutionSummary> counter;
    private final Color logColor;

    public String status() {
        return this.status;
    }

    public long countIn(final TestExecutionSummary summary) {
        return this.counter.applyAsLong(summary);
    }

    public Color logColor() {
        return this.logColor;
    }
}
