package com.knubisoft.testlum.testing.framework.xml;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that {@link XSDException} keeps its issues structured while exposing an already translated message.
 */
class XSDExceptionTest {

    private static final File FILE = new File("/scenarios/invalid/scenario.xml");

    @Test
    void messageIsTranslatedAndCarriesNoInternalCodes() {
        XSDException exception = new XSDException(FILE, List.of(
                issue("cvc-minLength-valid: Value 'Get all' with length = '7' is not facet-valid with respect "
                        + "to minLength '10' for type 'stringMin10'.", 16, 38),
                issue("cvc-attribute.3: The value 'Get all' of attribute 'comment' on element 'postgres' is not "
                        + "valid with respect to its type, 'stringMin10'.", 16, 38)));

        String message = exception.getMessage();
        assertTrue(message.contains("'comment' is too short: 7 characters, minimum is 10"), message);
        assertFalse(message.contains("cvc-"), message);
        assertFalse(message.contains("XSDIssue"), message);
    }

    @Test
    void messageDoesNotRepeatTheFilePath() {
        XSDException exception = new XSDException(FILE, List.of(
                issue("cvc-elt.1: Cannot find the declaration of element 'unknown'.", 1, 1)));
        assertFalse(exception.getMessage().contains(FILE.getPath()));
        assertEquals(FILE.getPath(), exception.getFile());
    }

    @Test
    void issuesStayAvailableForCallersThatWantToRenderThemDifferently() {
        XsdIssue first = issue("cvc-elt.1: Cannot find the declaration of element 'unknown'.", 1, 1);
        XSDException exception = new XSDException(FILE, List.of(first));
        assertEquals(1, exception.getIssues().size());
        assertSame(first, exception.getIssues().get(0));
    }

    @Test
    void issuesAreNotModifiableThroughTheException() {
        XSDException exception = new XSDException(FILE, List.of(
                issue("cvc-elt.1: Cannot find the declaration of element 'unknown'.", 1, 1)));
        assertThrows(UnsupportedOperationException.class, () -> exception.getIssues().clear());
    }

    @Test
    void readFailureKeepsCauseAndReportsNoIssues() {
        Exception cause = new IllegalStateException("boom");
        XSDException exception = new XSDException(FILE, "Cannot read scenario.xml. boom", cause);
        assertSame(cause, exception.getCause());
        assertTrue(exception.getIssues().isEmpty());
        assertNotNull(exception.getMessage());
    }

    private static XsdIssue issue(final String rawMessage, final int line, final int column) {
        return XsdIssue.of(XsdSeverity.ERROR,
                new org.xml.sax.SAXParseException(rawMessage, null, null, line, column));
    }
}
