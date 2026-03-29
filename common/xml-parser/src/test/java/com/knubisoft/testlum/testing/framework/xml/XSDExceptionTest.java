package com.knubisoft.testlum.testing.framework.xml;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link XSDException} verifying exception creation
 * from validation issues and message formatting.
 */
class XSDExceptionTest {

    @Test
    void createWithSingleIssue() {
        final Multimap<String, XSDException.XSDIssue> errors = ArrayListMultimap.create();
        errors.put("ERROR", new XSDException.XSDIssue("Invalid element", 10, 5, "/test.xml"));
        final XSDException exception = new XSDException(errors);
        assertNotNull(exception);
        assertNotNull(exception.getMessage());
    }

    @Test
    void createWithMultipleIssues() {
        final Multimap<String, XSDException.XSDIssue> errors = ArrayListMultimap.create();
        errors.put("ERROR", new XSDException.XSDIssue("Missing attr", 5, 3, "/a.xml"));
        errors.put("WARNING", new XSDException.XSDIssue("Deprecated", 12, 1, "/a.xml"));
        errors.put("FATAL_ERROR", new XSDException.XSDIssue("Malformed", 1, 1, "/b.xml"));
        final XSDException exception = new XSDException(errors);
        assertNotNull(exception.getMessage());
    }

    @Test
    void xsdIssueHoldsAllFields() {
        final XSDException.XSDIssue issue = new XSDException.XSDIssue("msg", 10, 5, "/path.xml");
        assertEquals("msg", issue.message);
        assertEquals(10, issue.lineNumber);
        assertEquals(5, issue.columnNumber);
        assertEquals("/path.xml", issue.path);
    }

    @Test
    void xsdIssueToStringContainsFields() {
        final XSDException.XSDIssue issue = new XSDException.XSDIssue("test msg", 3, 7, "/f.xml");
        final String str = issue.toString();
        assertTrue(str.contains("test msg"));
        assertTrue(str.contains("/f.xml"));
    }
}
