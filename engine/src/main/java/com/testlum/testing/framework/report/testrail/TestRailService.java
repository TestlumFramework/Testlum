package com.testlum.testing.framework.report.testrail;


import com.testlum.testing.framework.report.ScenarioResult;

import java.util.List;

public interface TestRailService {

    void generateTestRailReports(List<ScenarioResult> results);

}