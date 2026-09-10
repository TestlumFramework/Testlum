package com.testlum.testing.framework.report.testrail.model;

import com.testlum.testing.framework.report.ScenarioResult;

/**
 * A scenario result paired with the TestRail case id it must be reported to.
 * The id is resolved either from the testCaseId attribute or by
 * matching caseMatchKeyValue.
 */
public record ScenarioCase(ScenarioResult scenarioResult, int caseId) { }
