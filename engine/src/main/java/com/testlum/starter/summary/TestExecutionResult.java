package com.testlum.starter.summary;

import com.testlum.log.Color;
import lombok.RequiredArgsConstructor;

import java.util.function.ToLongFunction;

@RequiredArgsConstructor
public enum TestExecutionResult {

    FOUND("Found", ExecutionCounts::found, Color.CYAN),
    INVALID("Invalid", ExecutionCounts::invalid, Color.RED),
    SKIPPED("Skipped", ExecutionCounts::skipped, Color.ORANGE),
    SUCCESSFUL("Successful", ExecutionCounts::successful, Color.GREEN),
    FAILED("Failed", ExecutionCounts::failed, Color.RED);

    private final String status;
    private final ToLongFunction<ExecutionCounts> counter;
    private final Color logColor;

    public String status() {
        return this.status;
    }

    public long countIn(final ExecutionCounts counts) {
        return this.counter.applyAsLong(counts);
    }

    public Color logColor() {
        return this.logColor;
    }
}
