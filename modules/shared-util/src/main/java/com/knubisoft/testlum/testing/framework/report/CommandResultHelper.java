package com.knubisoft.testlum.testing.framework.report;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommandResultHelper {

    public static final String HEADER_TEMPLATE = "%s: %s";
    public static final String ADDITIONAL_HEADERS = "Additional headers";

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

    public static void addHeadersMetaData(final Map<String, String> headers, final CommandResult result) {
        result.put(ADDITIONAL_HEADERS, headers.entrySet().stream()
                .map(e -> String.format(HEADER_TEMPLATE, e.getKey(), e.getValue()))
                .toList());
    }
}
