package com.testlum.testing.framework.report.testrail.summary.dto;

/**
 * A scenario result that reached TestRail, paired with the case and run
 */
public record ReportedCase(String scenarioName, int caseId, int runId, boolean passed) { }
