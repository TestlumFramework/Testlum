package com.knubisoft.testlum.testing.framework.testRail;

import com.knubisoft.testlum.testing.framework.report.ScenarioResult;

import java.util.List;

public interface TestRailService {
    void sendTestResultToTestRail(List<ScenarioResult> results);
}
