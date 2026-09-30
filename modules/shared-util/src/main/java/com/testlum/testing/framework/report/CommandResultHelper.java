package com.testlum.testing.framework.report;

import com.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandResultHelper {

    public static final String HEADER_TEMPLATE = "%s: %s";
    public static final String ADDITIONAL_HEADERS = "Additional headers";

    public static final String EXPECTED_SOURCE = "Expected body file";
    public static final String ACTUAL_SOURCE = "Actual body file";

    private static final String STEP_FAILED = "Step failed";

    public static void setExceptionResult(final CommandResult result, final Exception exception) {
        result.setSuccess(false);
        result.setException(exception);
    }

    public static void setExecutionResultIfSubCommandsFailed(final CommandResult result) {
        List<CommandResult> subCommandsResult = result.getSubCommandsResult();
        if (subCommandsResult.stream().anyMatch(step -> !step.isSkipped() && !step.isSuccess())) {
            Exception exception = subCommandsResult.stream()
                    .filter(subCommand -> !subCommand.isSuccess())
                    .findFirst()
                    .map(CommandResult::getException)
                    .orElseGet(() -> new DefaultFrameworkException(STEP_FAILED));
            setExceptionResult(result, exception);
        }
    }

    public static void addComparisonSources(final CommandResult result, final String expectedFileOrContent,
                                            final String actualName) {
        if (isFileName(expectedFileOrContent)) {
            result.put(EXPECTED_SOURCE, expectedFileOrContent);
        }
        result.put(ACTUAL_SOURCE, actualName);
    }

    private static boolean isFileName(final String expectedFileOrContent) {
        return expectedFileOrContent != null && expectedFileOrContent.endsWith(".json");
    }

    public static void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> String.format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .toList());
    }
}
