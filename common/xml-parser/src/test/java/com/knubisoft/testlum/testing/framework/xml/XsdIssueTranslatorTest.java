package com.knubisoft.testlum.testing.framework.xml;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Covers the translation of the {@code cvc-*} codes the schemas can actually produce, plus the behaviour that
 * matters most when a code is not covered: degrade to the original sentence rather than lose the failure.
 */
class XsdIssueTranslatorTest {

    private static final String NAMESPACE = "http://www.knubisoft.com/testlum/testing/model/scenario";

    @Test
    void facetAndLocatorAtTheSamePositionBecomeOneProblem() {
        List<XsdProblem> problems = translate(
                error("cvc-minLength-valid: Value 'Get all' with length = '7' is not facet-valid with respect "
                        + "to minLength '10' for type 'stringMin10'.", 16, 38),
                error("cvc-attribute.3: The value 'Get all' of attribute 'comment' on element 'postgres' is not "
                        + "valid with respect to its type, 'stringMin10'.", 16, 38));

        assertEquals(1, problems.size());
        XsdProblem problem = problems.get(0);
        assertEquals(16, problem.line());
        assertEquals("<postgres comment=\"Get all\">", problem.context());
        assertEquals("'comment' is too short: 7 characters, minimum is 10", problem.problem());
    }

    /**
     * Every invalid attribute of one element is reported at that element's single position, so pairing has to
     * follow the order Xerces emits in. Matching on position alone would collapse the first pair and leave the
     * others reported twice - once as a rule, once as a location.
     */
    @Test
    void everyBadAttributeOfOneElementCollapsesIntoItsOwnProblem() {
        List<XsdProblem> problems = translate(
                error("cvc-pattern-valid: Value 'bad alias!' is not facet-valid with respect to pattern "
                        + "'[a-zA-Z_\\-/\\d]+' for type 'aliasPattern'.", 9, 5),
                error("cvc-attribute.3: The value 'bad alias!' of attribute 'alias' on element 'postgres' is "
                        + "not valid with respect to its type, 'aliasPattern'.", 9, 5),
                error("cvc-minLength-valid: Value 'short' with length = '5' is not facet-valid with respect "
                        + "to minLength '10' for type 'stringMin10'.", 9, 5),
                error("cvc-attribute.3: The value 'short' of attribute 'comment' on element 'postgres' is not "
                        + "valid with respect to its type, 'stringMin10'.", 9, 5));

        assertEquals(2, problems.size());
        assertEquals("<postgres alias=\"bad alias!\">", problems.get(0).context());
        assertEquals("<postgres comment=\"short\">", problems.get(1).context());
        assertEquals("'comment' is too short: 5 characters, minimum is 10", problems.get(1).problem());
    }

    /**
     * Two mistakes on the same element land on the same line but different columns, so they must stay apart.
     */
    @Test
    void issuesAtDifferentPositionsStayApart() {
        List<XsdProblem> problems = translate(
                error("cvc-complex-type.4: Attribute 'comment' must appear on element 'postgres'.", 16, 20),
                error("cvc-complex-type.4: Attribute 'file' must appear on element 'postgres'.", 16, 40));
        assertEquals(2, problems.size());
    }

    @Test
    void warningsNeverBecomeProblems() {
        List<XsdProblem> problems = XsdIssueTranslator.translate(List.of(
                XsdIssue.of(XsdSeverity.WARNING,
                        new SAXParseException("cvc-elt.1: Cannot find the declaration of element 'x'.",
                                null, null, 2, 2))));
        assertTrue(problems.isEmpty());
    }

    @Test
    void tooLongValueIsReported() {
        XsdProblem problem = single(
                error("cvc-maxLength-valid: Value 'abc' with length = '3' is not facet-valid with respect to "
                        + "maxLength '2' for type 'shortString'.", 4, 4));
        assertEquals("Value 'abc' is too long: 3 characters, maximum is 2", problem.problem());
    }

    @Test
    void numericBoundsAreReported() {
        assertEquals("Value '0' must be 1 or greater", single(
                error("cvc-minInclusive-valid: Value '0' is not facet-valid with respect to minInclusive '1' "
                        + "for type 'positiveIntegerMin1'.", 5, 5)).problem());
        assertEquals("Value '900' must be 599 or less", single(
                error("cvc-maxInclusive-valid: Value '900' is not facet-valid with respect to maxInclusive "
                        + "'599' for type 'codePattern'.", 6, 6)).problem());
    }

    @Test
    void wrongDatatypeIsSpelledOutInPlainWords() {
        XsdProblem problem = single(
                error("cvc-datatype-valid.1.2.1: 'abc' is not a valid value for 'integer'.", 7, 7));
        assertEquals("Value 'abc' is not a valid whole number", problem.problem());
    }

    /**
     * A pattern is a regexp - showing it to a test author explains nothing, so the sentence names the value
     * and stops there.
     */
    @Test
    void patternViolationNamesTheValueWithoutTheRegexp() {
        XsdProblem problem = single(
                error("cvc-pattern-valid: Value 'result.txt' is not facet-valid with respect to pattern "
                        + "'expected_\\d+.json' for type 'expectedPattern'.", 8, 8));
        assertEquals("Value 'result.txt' has an unexpected format", problem.problem());
    }

    /**
     * A scenario choice lists roughly thirty five commands; printed in full the sentence is longer than the
     * console is wide, and it repeats the namespace on every entry.
     */
    @Test
    void longExpectedElementListIsShortenedAndUnqualified() {
        String expected = IntStream.range(0, 12)
                .mapToObj(index -> String.format("\"%s\":command%d", NAMESPACE, index))
                .collect(Collectors.joining(", ", "{", "}"));
        XsdProblem problem = single(
                error(String.format("cvc-complex-type.2.4.a: Invalid content was found starting with element "
                        + "'\"%s\":web'. One of '%s' is expected.", NAMESPACE, expected), 40, 9));

        assertTrue(problem.problem().startsWith("<web> cannot be used here. Allowed: command0, command1"),
                problem.problem());
        assertTrue(problem.problem().endsWith("... and 4 more"), problem.problem());
        assertFalse(problem.problem().contains(NAMESPACE), problem.problem());
    }

    /**
     * A long value is shown once, abbreviated, on the context line - never spelled out again at full length in
     * the sentence below it, where nothing would keep it inside the column.
     */
    @Test
    void longAttributeValuesAreShownOnceAndAbbreviated() {
        String value = "a-very-long-and-completely-wrong-expected-file-name.txt";
        XsdProblem problem = translate(
                error("cvc-pattern-valid: Value '" + value + "' is not facet-valid with respect to pattern "
                        + "'expected_\\d+.json' for type 'expectedPattern'.", 9, 5),
                error("cvc-attribute.3: The value '" + value + "' of attribute 'file' on element 'postgres' "
                        + "is not valid with respect to its type, 'expectedPattern'.", 9, 5)).get(0);

        assertEquals("'file' has an unexpected format", problem.problem());
        assertFalse(problem.context().contains(value), problem.context());
        assertTrue(problem.context().endsWith("...\">"), problem.context());
    }

    @Test
    void structuralProblemsNeedNoContextLineBecauseTheyNameTheirElement() {
        XsdProblem problem = single(
                error("cvc-complex-type.4: Attribute 'comment' must appear on element 'postgres'.", 22, 9));
        assertEquals("", problem.context());
        assertEquals("<postgres> is missing required attribute 'comment'", problem.problem());
    }

    @Test
    void unknownCodeKeepsTheOriginalSentenceWithoutItsPrefix() {
        XsdProblem problem = single(error("cvc-brand-new.9.9: Something we never mapped happened.", 3, 3));
        assertEquals("Something we never mapped happened.", problem.problem());
    }

    @Test
    void messageWithoutAnyCodeIsPassedThroughUnchanged() {
        String raw = "The element type \"postgres\" must be terminated by the matching end-tag.";
        assertEquals(raw, single(error(raw, 12, 1)).problem());
    }

    private static XsdProblem single(final XsdIssue issue) {
        List<XsdProblem> problems = XsdIssueTranslator.translate(List.of(issue));
        assertEquals(1, problems.size());
        return problems.get(0);
    }

    private static List<XsdProblem> translate(final XsdIssue... issues) {
        return XsdIssueTranslator.translate(List.of(issues));
    }

    private static XsdIssue error(final String rawMessage, final int line, final int column) {
        return XsdIssue.of(XsdSeverity.ERROR, new SAXParseException(rawMessage, null, null, line, column));
    }
}
