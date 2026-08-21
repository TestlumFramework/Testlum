package com.knubisoft.testlum.testing.framework.report.testrails.util;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;
import com.knubisoft.testlum.testing.framework.report.testrails.constant.TestRailConstants;
import com.knubisoft.testlum.testing.framework.report.testrails.model.ResultRequestDto;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

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

    private  String sanitizeFailureMessage(final String cause) {
        if (StringUtils.isBlank(cause)) {
            return "";
        }
        String msg = cause.replace('\r', '\n');
        int idx = msg.lastIndexOf("Caused by:");
        if (idx >= 0) {
            msg = msg.substring(idx + "Caused by:".length()).trim();
        }
        msg = STACK_TRACE_LINE.matcher(msg).replaceAll("");
        msg = EX_PREFIX.matcher(msg).replaceFirst("");
        msg = msg.replace('\n', ' ').replaceAll("\\s+", " ").trim();
        return msg;
    }

    public String durationOfExecution(long executionTime) {
        long totalSeconds = executionTime / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        StringBuilder duration = new StringBuilder();
        if (hours > 0) {
            duration.append(hours).append("h ");
        }
        if (minutes > 0) {
            duration.append(minutes).append("m ");
        }
        if (seconds > 0) {
            duration.append(seconds).append("s");
        }

        return duration.toString().trim();
    }

}
