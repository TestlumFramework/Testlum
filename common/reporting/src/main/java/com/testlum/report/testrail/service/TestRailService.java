package com.testlum.report.testrail.service;


import com.testlum.report.ScenarioResult;

import java.util.List;

public interface TestRailService {

    void generateTestRailReports(List<ScenarioResult> results);

}