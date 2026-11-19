package com.testlum.testing.framework.report;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * Names the step that failed a scenario: a short label for the report message and labeled lines for the report text.
 * For include, repeat and assert wrappers the innermost failing step is the one named.
 */
public record FailedStep(String label, String text) {

    private static final String BEFORE_FIRST_STEP = "before step 1";

    /** Describes the failure of {@code top}, a top-level step of the scenario. */
    public static FailedStep of(final CommandResult top, final Throwable error) {
        CommandResult leaf = innermostFailure(top);
        long step = top.getId() > 0 ? top.getId() : leaf.getId();
        String command = StringUtils.defaultIfBlank(leaf.getCommandKey(), "unknown");
        StringBuilder text = new StringBuilder();
        line(text, "step", String.valueOf(step));
        line(text, "inner step", leaf != top && leaf.getId() != step ? String.valueOf(leaf.getId()) : null);
        line(text, "command", command);
        line(text, "comment", StringUtils.firstNonBlank(leaf.getComment(), top.getComment()));
        line(text, "error-type", errorType(leaf, error));
        line(text, "http", httpLine(leaf));
        return new FailedStep("step " + step + " " + command, text.toString());
    }

    private static CommandResult innermostFailure(final CommandResult top) {
        CommandResult leaf = top;
        for (CommandResult next = firstFailedChild(leaf); next != null; next = firstFailedChild(leaf)) {
            leaf = next;
        }
        return leaf;
    }

    private static String errorType(final CommandResult leaf, final Throwable error) {
        Throwable cause = leaf.getException() != null ? leaf.getException() : error;
        return cause == null ? null : cause.getClass().getSimpleName();
    }

    private static String httpLine(final CommandResult leaf) {
        Object expected = leaf.getMetadata().get(CommandResultHelper.EXPECTED_HTTP_STATUS);
        Object actual = leaf.getMetadata().get(CommandResultHelper.HTTP_STATUS);
        return expected == null || actual == null ? null : "expected status " + expected + ", actual " + actual;
    }

    /** Describes a failure that happened before the first step ran. */
    public static FailedStep beforeFirstStep(final Throwable error) {
        StringBuilder text = new StringBuilder();
        line(text, "step", BEFORE_FIRST_STEP);
        line(text, "error-type", error.getClass().getSimpleName());
        return new FailedStep(BEFORE_FIRST_STEP, text.toString());
    }

    private static CommandResult firstFailedChild(final CommandResult parent) {
        List<CommandResult> children = parent.getSubCommandsResult();
        if (children == null) {
            return null;
        }
        return children.stream().filter(c -> !c.isSuccess() && !c.isSkipped()).findFirst().orElse(null);
    }

    private static void line(final StringBuilder text, final String name, final String value) {
        if (StringUtils.isNotBlank(value)) {
            text.append(name).append(": ").append(value.trim().replaceAll("\\s+", " ")).append('\n');
        }
    }
}
