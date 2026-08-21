package com.knubisoft.testlum.testing.framework.xml;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

public final class XsdIssueFormatter {

    private static final String LINE_BREAK = "\n";

    private static final String BLOCK_SEPARATOR = "\n\n";

    private static final String HEADER = "line %d  %s";

    private static final String PROBLEM_LINE = "   x  %s";

    private XsdIssueFormatter() {
    }

    public static String format(final List<XsdIssue> issues) {
        return formatProblems(XsdIssueTranslator.translate(issues));
    }

    public static String formatProblems(final List<XsdProblem> problems) {
        return problems.stream()
                .map(XsdIssueFormatter::formatProblem)
                .collect(Collectors.joining(BLOCK_SEPARATOR));
    }

    private static String formatProblem(final XsdProblem problem) {
        if (StringUtils.isEmpty(problem.context())) {
            return String.format(HEADER, problem.line(), problem.problem());
        }
        return String.format(HEADER, problem.line(), problem.context())
                + LINE_BREAK
                + String.format(PROBLEM_LINE, problem.problem());
    }
}
