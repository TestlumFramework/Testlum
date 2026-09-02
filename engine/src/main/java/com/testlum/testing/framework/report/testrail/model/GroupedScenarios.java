package com.testlum.testing.framework.report.testrail.model;

import com.testlum.testing.framework.report.ScenarioResult;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class GroupedScenarios {
    private final Map<Integer, List<ScenarioResult>> withRunId;
    private final List<ScenarioResult> withoutRunId;
}