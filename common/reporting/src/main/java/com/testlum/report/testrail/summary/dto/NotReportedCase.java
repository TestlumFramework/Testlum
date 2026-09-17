package com.testlum.report.testrail.summary.dto;

/**
 * A scenario result that did not reach TestRail,
 */
public record NotReportedCase(String scenarioName, String caseId, String reason) { }
