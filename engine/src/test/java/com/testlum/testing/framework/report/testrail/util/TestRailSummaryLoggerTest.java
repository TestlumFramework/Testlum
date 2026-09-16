package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.summary.TestRailReportSummary;
import com.testlum.testing.framework.report.testrail.summary.TestRailSummaryLogger;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class TestRailSummaryLoggerTest {

    private final TestRailSummaryLogger summaryLogger = new TestRailSummaryLogger();

    private static String plain(final String table) {
        return table.replaceAll("\\[[;\\d]*m", "");
    }

    @Nested
    class ReportedTable {

        @Test
        void holdsScenarioCaseRunAndResultOfEveryReportedCase() {
            TestRailReportSummary summary = new TestRailReportSummary();
            summary.addReported("login", 91, 300, true);
            summary.addReported("login", 94, 300, true);
            summary.addReported("checkout", 77, 301, false);

            String table = plain(summaryLogger.buildReportedTable(summary));

            assertTrue(table.contains(TestRailConstants.REPORTED_TABLE_TITLE), table);
            assertTrue(table.contains("login"), table);
            assertTrue(table.contains("91"), table);
            assertTrue(table.contains("94"), table);
            assertTrue(table.contains("300"), table);
            assertTrue(table.contains(TestRailConstants.RESULT_PASSED), table);
            assertTrue(table.contains(TestRailConstants.RESULT_FAILED), table);
        }

        @Test
        void countsSentResultsAndAttachedScreenshotsInFooter() {
            TestRailReportSummary summary = new TestRailReportSummary();
            summary.addReported("login", 91, 300, true);
            summary.addReported("checkout", 77, 300, false);
            summary.addAttachedScreenshots(1);

            String table = plain(summaryLogger.buildReportedTable(summary));

            assertTrue(table.contains(String.format(TestRailConstants.REPORTED_TABLE_FOOTER, 2, 1)), table);
        }
    }

    @Nested
    class NotReportedTable {

        @Test
        void holdsScenarioCaseAndReasonOfEveryCaseThatWasNotReported() {
            TestRailReportSummary summary = new TestRailReportSummary();
            summary.addNotReported("login", TestRailConstants.CASE_ID_UNRESOLVED,
                    TestRailConstants.REASON_CASE_REFERENCE_MISSING);
            summary.addNotReported("checkout", "C94", TestRailConstants.REASON_CASE_ID_NOT_PARSABLE);

            String table = plain(summaryLogger.buildNotReportedTable(summary));

            assertTrue(table.contains(TestRailConstants.NOT_REPORTED_TABLE_TITLE), table);
            assertTrue(table.contains("C94"), table);
            assertTrue(table.contains(TestRailConstants.REASON_CASE_ID_NOT_PARSABLE), table);
            assertTrue(table.contains(String.format(TestRailConstants.NOT_REPORTED_TABLE_FOOTER, 2)), table);
        }
    }
}
