package com.testlum.testing.report.testrail.service;


import com.testlum.testing.report.ScenarioResult;

import java.util.List;

public interface TestRailService {

    void generateTestRailReports(List<ScenarioResult> results);

}