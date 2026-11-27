package com.testlum.starter.failure;

import com.testlum.testing.framework.xml.XSDException;
import com.testlum.testing.framework.xml.XsdIssue;
import com.testlum.testing.framework.xml.XsdSeverity;
import org.junit.jupiter.api.Test;
import org.xml.sax.SAXParseException;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Configuration files are validated before the invalid scenarios table exists, so their schema failures surface
 * here instead. The report has to name the file and stay readable.
 */
class StartupFailureReporterTest {

    private static final File CONFIG = new File("/demo/resources/global-config.xml");

    private static final String HEADLINE = "Testlum failed to start";

    @Test
    void schemaFailureIsReportedWithItsFileAndTranslatedMessage() {
        String report = reportOf(schemaFailure());
        assertTrue(report.contains(CONFIG.getPath()), report);
        assertTrue(report.contains("<environmentz> cannot be used here. Allowed: environments"), report);
    }

    /**
     * The translated message already says which file, which line and what to write instead; a Xerces stacktrace
     * under it would only bury that.
     */
    @Test
    void schemaFailureIsReportedWithoutAStacktrace() {
        String report = reportOf(schemaFailure());
        assertFalse(report.contains("Full stacktrace:"), report);
        assertFalse(report.contains("cvc-"), report);
    }

    /**
     * For every other failure the stacktrace is the diagnosis, so it must still be printed.
     */
    @Test
    void otherFailuresKeepTheirStacktrace() {
        String report = reportOf(new IllegalStateException("connection refused"));
        assertTrue(report.contains("Full stacktrace:"), report);
        assertTrue(report.contains("connection refused"), report);
    }

    @Test
    void aReportedFailureIsRemembered() {
        RuntimeException failure = new IllegalStateException("boom");
        StartupFailureReporter.report(HEADLINE, failure);
        assertTrue(StartupFailureReporter.wasReported(failure));
    }

    private static String reportOf(final Throwable failure) {
        StartupFailureReporter.report(HEADLINE, failure);
        return describeThrough(failure);
    }

    /**
     * {@link StartupFailureReporter} logs rather than returns, so the report is rebuilt through the same
     * private path to assert on it.
     */
    private static String describeThrough(final Throwable failure) {
        try {
            var method = StartupFailureReporter.class.getDeclaredMethod("constructReport", String.class,
                    Throwable.class);
            method.setAccessible(true);
            return String.valueOf(method.invoke(null, HEADLINE, failure));
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static XSDException schemaFailure() {
        XsdIssue issue = XsdIssue.of(XsdSeverity.ERROR, new SAXParseException(
                "cvc-complex-type.2.4.a: Invalid content was found starting with element "
                        + "'\"urn:global-config\":environmentz'. One of '{\"urn:global-config\":environments}' "
                        + "is expected.", null, null, 27, 5));
        return new XSDException(CONFIG, List.of(issue));
    }
}
