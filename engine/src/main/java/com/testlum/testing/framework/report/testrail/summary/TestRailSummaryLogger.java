package com.testlum.testing.framework.report.testrail.summary;

import com.testlum.log.Color;
import com.testlum.log.table.Align;
import com.testlum.log.table.DynamicTableBuilder;
import com.testlum.log.table.TableBuilder;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.summary.dto.NotReportedCase;
import com.testlum.testing.framework.report.testrail.summary.dto.ReportedCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Renders TestRail reporting table
 */
@Slf4j
@Component
public class TestRailSummaryLogger {

    public void logSummary(final TestRailReportSummary summary) {
        if (!summary.getReportedCases().isEmpty()) {
            log.info(buildReportedTable(summary));
        }
        if (!summary.getNotReportedCases().isEmpty()) {
            log.error(buildNotReportedTable(summary));
        }
    }

    public String buildReportedTable(final TestRailReportSummary summary) {
        DynamicTableBuilder table = TableBuilder.grid(TestRailConstants.REPORTED_TABLE_TITLE)
                .columns(TestRailConstants.SCENARIO_HEADER, TestRailConstants.CASE_HEADER,
                        TestRailConstants.RUN_HEADER, TestRailConstants.RESULT_HEADER);
        summary.getReportedCases().forEach(reportedCase -> addReportedRow(table, reportedCase));
        return table.footer(String.format(TestRailConstants.REPORTED_TABLE_FOOTER,
                        summary.getReportedCases().size(), summary.getAttachedScreenshots()))
                .align(Align.LEFT)
                .color(Color.CYAN)
                .build();
    }

    public String buildNotReportedTable(final TestRailReportSummary summary) {
        DynamicTableBuilder table = TableBuilder.grid(TestRailConstants.NOT_REPORTED_TABLE_TITLE)
                .columns(TestRailConstants.SCENARIO_HEADER, TestRailConstants.CASE_HEADER,
                        TestRailConstants.REASON_HEADER);
        summary.getNotReportedCases().forEach(notReportedCase ->
                addNotReportedRow(table, notReportedCase));
        return table.footer(String.format(TestRailConstants.NOT_REPORTED_TABLE_FOOTER,
                        summary.getNotReportedCases().size()))
                .align(Align.LEFT)
                .color(Color.RED)
                .build();
    }

    private void addReportedRow(final DynamicTableBuilder table, final ReportedCase reportedCase) {
        table.row(reportedCase.passed() ? Color.GREEN : Color.RED,
                reportedCase.scenarioName(), reportedCase.caseId(), reportedCase.runId(),
                reportedCase.passed() ? TestRailConstants.RESULT_PASSED : TestRailConstants.RESULT_FAILED);
    }

    private void addNotReportedRow(final DynamicTableBuilder table, final NotReportedCase notReportedCase) {
        table.row(notReportedCase.scenarioName(), notReportedCase.caseId(), notReportedCase.reason());
    }
}
