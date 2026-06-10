package com.knubisoft.testlum.starter.summary;

import com.knubisoft.testlum.log.Color;
import com.knubisoft.testlum.log.LogFormat;
import com.knubisoft.testlum.log.table.DynamicTableBuilder;
import com.knubisoft.testlum.log.table.TableBuilder;
import com.knubisoft.testlum.testing.framework.constant.LogMessage;
import lombok.extern.slf4j.Slf4j;
import org.junit.platform.launcher.listeners.TestExecutionSummary;
import org.springframework.stereotype.Component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@Component
public class TestExecutionPostProcessor {

    private static final int SECONDS_IN_MINUTE = 60;

    public void process(final TestExecutionSummary summary, final Color logColor, final String message) {
        this.logResultTable(summary, logColor, message);
        this.logFailures(summary);
    }

    private void logResultTable(final TestExecutionSummary summary, final Color logColor, final String message) {
        DynamicTableBuilder tableBuilder = TableBuilder.grid(message)
                .titleColor(logColor)
                .columns("Status", "Counts")
                .footer(logColor, this.computeResultFooter(summary));
        for (TestExecutionResult result : TestExecutionResult.values()) {
            tableBuilder.row(result.logColor(), result.status(), result.countIn(summary));
        }
        log.info(tableBuilder.build());
    }

    private void logFailures(final TestExecutionSummary summary) {
        if (summary.getTestsFailedCount() == 0 || summary.getFailures().isEmpty()) {
            return;
        }
        for (TestExecutionSummary.Failure failure : summary.getFailures()) {
            log.error(LogFormat.withRed(failure.getTestIdentifier().getDisplayName()));
            log.error(LogFormat.withRed(failure.getException().getMessage()));
            log.error(LogFormat.withRed(Arrays.toString(failure.getException().getStackTrace())));
            log.error(LogFormat.withRed(LogMessage.LINE));
        }
    }

    private String computeResultFooter(final TestExecutionSummary summary) {
        long executionTimeInMs = summary.getTimeFinished() - summary.getTimeStarted();
        long minutes = TimeUnit.MILLISECONDS.toMinutes(executionTimeInMs);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(executionTimeInMs) % SECONDS_IN_MINUTE;
        return String.format("Test run finished after %dm %ds", minutes, seconds);
    }

    /**
     * Converts a PrintWriter consumer output to a String.
     *
     * @param writer consumer that writes to a PrintWriter
     * @return the written content as a string
     */
    private static String toString(final Consumer<PrintWriter> writer) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);
        writer.accept(printWriter);
        return stringWriter.toString();
    }

}
