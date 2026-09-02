package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.ResultRequestDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class TestRailResultMapper {

    private static final Pattern EX_PREFIX = Pattern.compile(
            "^(?:(?:[\\p{Alpha}_$][\\w$]*\\.)*[\\p{Alpha}_$][\\w$]*(?:Exception|Error|Failure|Throwable)\\s*:\\s*)+"
    );
    private static final Pattern STACK_TRACE_LINE = Pattern.compile(
            "(?m)^\\s*(?:at\\s+.+|Suppressed:\\s+.+|\\.\\.\\.\\s+\\d+\\s+more)\\s*$"
    );

    public ResultRequestDto formatResult(final ScenarioResult scenarioResult) {
        return new ResultRequestDto(
                scenarioResult.getOverview().getTestRail().getTestCaseId(),
                determineStatus(scenarioResult),
                generateComment(scenarioResult),
                durationOfExecution(scenarioResult.getExecutionTime())
        );
    }

    private int determineStatus(final ScenarioResult scenarioResult) {
        return scenarioResult.isSuccess()
                ? TestRailConstants.STATUS_PASSED
                : TestRailConstants.STATUS_FAILED;
    }

    private String generateComment(final ScenarioResult scenarioResult) {
        String scenarioName = scenarioResult.getOverview().getName();
        String cause = sanitizeFailureMessage(scenarioResult.getCause());
        return StringUtils.isNotBlank(cause)
                ? String.format(TestRailConstants.COMMENT_FAILED_TEMPLATE, scenarioName, cause)
                : String.format(TestRailConstants.COMMENT_PASSED_TEMPLATE, scenarioName);
    }

    private String sanitizeFailureMessage(final String cause) {
        if (StringUtils.isBlank(cause)) {
            return "";
        }
        String errorMessage = cause.replace('\r', '\n');
        int idx = errorMessage.lastIndexOf("Caused by:");
        if (idx >= 0) {
            errorMessage = errorMessage.substring(idx + "Caused by:".length()).trim();
        }
        errorMessage = STACK_TRACE_LINE.matcher(errorMessage).replaceAll("");
        errorMessage = EX_PREFIX.matcher(errorMessage).replaceFirst("");
        errorMessage = errorMessage.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        return errorMessage;
    }

    public String durationOfExecution(final long executionTime) {
        Duration duration = Duration.ofMillis(executionTime);
        List<String> parts = new ArrayList<>();
        addIfPositive(parts, duration.toHours(), "h");
        addIfPositive(parts, duration.toMinutesPart(), "m");
        addIfPositive(parts, duration.toSecondsPart(), "s");
        return String.join(" ", parts);
    }

    private void addIfPositive(final List<String> parts, final long value, final String unit) {
        if (value > 0) {
            parts.add(value + unit);
        }
    }
}
