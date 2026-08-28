package com.testlum.starter.summary;

import com.testlum.log.Color;
import com.testlum.log.table.DynamicTableBuilder;
import com.testlum.log.table.TableBuilder;
import com.testlum.starter.failure.StartupFailureReporter;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class TestExecutionPostProcessor {

    private static final int SECONDS_IN_MINUTE = 60;

    public void process(final TestExecutionSummary summary,
                        final ExecutionCounts counts,
                        final Color logColor,
                        final String message) {
        this.logResultTable(summary, counts, logColor, message);
        this.logFailures(summary);
    }

    private void logResultTable(final TestExecutionSummary summary,
                                final ExecutionCounts counts,
                                final Color logColor,
                                final String message) {
        DynamicTableBuilder tableBuilder = TableBuilder.grid(message)
                .titleColor(logColor)
                .columns("Status", "Counts")
                .footer(logColor, this.computeResultFooter(summary));
        for (TestExecutionResult result : TestExecutionResult.values()) {
            tableBuilder.row(result.logColor(), result.status(), result.countIn(counts));
        }
        log.info(tableBuilder.build());
    }

    private void logFailures(final TestExecutionSummary summary) {
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            if (StartupFailureReporter.wasReported(failure.getException())) {
                continue;
            }
            StartupFailureReporter.report(failure.getTestIdentifier().getDisplayName(), failure.getException());
        }
    }

    private String computeResultFooter(final TestExecutionSummary summary) {
        long executionTimeInMs = summary.getTimeFinished() - summary.getTimeStarted();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTimeInMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTimeInMs) % SECONDS_IN_MINUTE;
        return String.format("Test run finished after %dm %ds", minutes, seconds);
    }

}
