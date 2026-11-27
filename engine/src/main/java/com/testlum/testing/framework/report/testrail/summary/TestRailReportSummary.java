package com.testlum.testing.framework.report.testrail.summary;

import com.testlum.testing.framework.report.testrail.summary.dto.NotReportedCase;
import com.testlum.testing.framework.report.testrail.summary.dto.ReportedCase;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Collects what happened to every scenario handed to TestRail during a run
 */
@Getter
public class TestRailReportSummary {

    private final List<ReportedCase> reportedCases = new ArrayList<>();
    private final List<NotReportedCase> notReportedCases = new ArrayList<>();
    private int attachedScreenshots;

    public void addReported(final String scenarioName, final int caseId, final int runId, final boolean passed) {
        this.reportedCases.add(new ReportedCase(scenarioName, caseId, runId, passed));
    }

    public void addNotReported(final String scenarioName, final String caseId, final String reason) {
        this.notReportedCases.add(new NotReportedCase(scenarioName, caseId, reason));
    }

    public void addAttachedScreenshots(final int screenshots) {
        this.attachedScreenshots += screenshots;
    }

    public boolean isEmpty() {
        return this.reportedCases.isEmpty() && this.notReportedCases.isEmpty();
    }
}
