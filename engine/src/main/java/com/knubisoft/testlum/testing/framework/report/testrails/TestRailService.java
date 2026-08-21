package com.knubisoft.testlum.testing.framework.report.testrails;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;

import java.util.List;

public interface TestRailService {

    void generateTestRailReports(List<ScenarioResult> results);

}
