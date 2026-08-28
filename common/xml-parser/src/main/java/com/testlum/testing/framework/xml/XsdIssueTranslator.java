package com.testlum.testing.framework.xml;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class XsdIssueTranslator {

    private static final Set<String> LOCATOR_CODES = Set.of("cvc-attribute.3", "cvc-type.3.1.3", "cvc-elt.4.2");

    private static final String ATTRIBUTE_CODE = "cvc-attribute.3";

    private static final String ATTRIBUTE_SUBJECT = "'%s'";

    private static final String VALUE_SUBJECT = "Value '%s'";

    private static final String FALLBACK_SUBJECT = "Value";

    private static final String ATTRIBUTE_CONTEXT = "<%s %s=\"%s\">";

    private static final int MAX_VALUE_LENGTH = 40;

    private XsdIssueTranslator() {
    }

    public static List<XsdProblem> translate(final List<XsdIssue> issues) {
        List<XsdIssue> blocking = issues.stream().filter(XsdIssue::blocksValidation).toList();
        List<XsdProblem> problems = new ArrayList<>();
        int index = 0;
        while (index < blocking.size()) {
            XsdIssue locator = locatorFollowing(blocking, index);
            problems.add(toProblem(blocking.get(index), locator));
            index += locator == null ? 1 : 2;
        }
        return problems;
    }

    private static XsdIssue locatorFollowing(final List<XsdIssue> issues, final int index) {
        int next = index + 1;
        XsdIssue facet = issues.get(index);
        if (!XsdMessages.isFacet(facet.code()) || next >= issues.size()) {
            return null;
        }
        XsdIssue candidate = issues.get(next);
        boolean samePlace = candidate.line() == facet.line() && candidate.column() == facet.column();
        boolean sameValue = Objects.equals(candidate.arg(0), facet.arg(0));
        return samePlace && sameValue && LOCATOR_CODES.contains(candidate.code()) ? candidate : null;
    }

    private static XsdProblem toProblem(final XsdIssue issue, final XsdIssue locator) {
        return new XsdProblem(issue.line(),
                contextOf(locator),
                XsdMessages.render(issue, subjectOf(issue, locator)));
    }

    private static String contextOf(final XsdIssue locator) {
        if (locator == null || !ATTRIBUTE_CODE.equals(locator.code())) {
            return StringUtils.EMPTY;
        }
        return String.format(ATTRIBUTE_CONTEXT, locator.arg(2), locator.arg(1), shorten(locator.arg(0)));
    }

    private static String subjectOf(final XsdIssue issue, final XsdIssue locator) {
        if (locator != null && ATTRIBUTE_CODE.equals(locator.code()) && locator.arg(1) != null) {
            return String.format(ATTRIBUTE_SUBJECT, locator.arg(1));
        }
        String value = issue.arg(0);
        return value == null ? FALLBACK_SUBJECT : String.format(VALUE_SUBJECT, shorten(value));
    }

    private static String shorten(final String value) {
        return StringUtils.abbreviate(value, MAX_VALUE_LENGTH);
    }
}
