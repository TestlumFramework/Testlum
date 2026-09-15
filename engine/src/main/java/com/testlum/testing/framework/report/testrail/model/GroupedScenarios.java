package com.testlum.testing.framework.report.testrail.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@RequiredArgsConstructor
public class GroupedScenarios {
    private final Map<Integer, List<ScenarioCase>> withRunId;
    private final List<ScenarioCase> withoutRunId;
}