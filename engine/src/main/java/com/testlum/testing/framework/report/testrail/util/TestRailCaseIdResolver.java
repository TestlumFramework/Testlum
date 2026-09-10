package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.model.ScenarioCase;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.TestRailReports;
import com.testlum.testing.model.scenario.TestRail;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves the TestRail case each scenario must be reported against.
 * A scenario either names the case directly via testCaseId, or names a value of
 * the custom field configured as caseMatchKey via caseMatchKeyValue.
 * Scenarios whose case cannot be resolved are logged and dropped rather than failing the run.
 */
@Slf4j
@Component
public class TestRailCaseIdResolver {

    private final TestRailApiClient testRailApiClient;
    private final TestRailReports testRails;

    public TestRailCaseIdResolver(final GlobalTestConfiguration globalTestConfiguration,
                                  final TestRailApiClient testRailApiClient) {
        this.testRailApiClient = testRailApiClient;
        this.testRails = globalTestConfiguration.getReport().getExtentReports().getTestRailReports();
    }

    public List<ScenarioCase> resolveCases(final List<ScenarioResult> scenarioResults) {
        List<ScenarioCase> resolvedCases = new ArrayList<>();
        List<ScenarioResult> matchedByKey = new ArrayList<>();
        scenarioResults.forEach(scenarioResult ->
                sortByResolutionFlow(scenarioResult, resolvedCases, matchedByKey));
        resolvedCases.addAll(resolveByMatchKey(matchedByKey));
        return resolvedCases;
    }

    private void sortByResolutionFlow(final ScenarioResult scenarioResult,
                                      final List<ScenarioCase> resolved,
                                      final List<ScenarioResult> matchedByKey) {
        TestRail testRail = scenarioResult.getOverview().getTestRail();
        if (hasUsableCaseId(testRail)) {
            resolved.add(new ScenarioCase(scenarioResult, Integer.parseInt(testRail.getTestCaseId().trim())));
        } else if (StringUtils.isNotBlank(testRail.getCaseMatchKeyValue())) {
            matchedByKey.add(scenarioResult);
        } else {
            log.error(TestRailConstants.TEST_RAIL_CASE_NOT_RESOLVED, scenarioResult.getName());
        }
    }

    private boolean hasUsableCaseId(final TestRail testRail) {
        String testCaseId = testRail.getTestCaseId();
        return StringUtils.isNotBlank(testCaseId)
                && NumberUtils.isParsable(testCaseId.trim())
                && Integer.parseInt(testCaseId.trim()) > 0;
    }

    private List<ScenarioCase> resolveByMatchKey(final List<ScenarioResult> scenarioResults) {
        if (scenarioResults.isEmpty()) {
            return List.of();
        }
        String caseMatchKey = testRails.getCaseMatchKey();
        if (StringUtils.isBlank(caseMatchKey)) {
            logMatchKeyNotConfigured(scenarioResults);
            return List.of();
        }
        Map<String, Integer> caseIdsByMatchKeyValue = testRailApiClient.fetchCaseIdsByMatchKey(caseMatchKey.trim());
        List<ScenarioCase> resolvedCases = new ArrayList<>();
        scenarioResults.forEach(scenarioResult ->
                matchCase(scenarioResult, caseMatchKey, caseIdsByMatchKeyValue).ifPresent(resolvedCases::add));
        return resolvedCases;
    }

    private void logMatchKeyNotConfigured(final List<ScenarioResult> scenarioResults) {
        scenarioResults.forEach(scenarioResult ->
                log.error(TestRailConstants.CASE_MATCH_KEY_NOT_CONFIGURED, scenarioResult.getName()));
    }

    private Optional<ScenarioCase> matchCase(final ScenarioResult scenarioResult,
                                             final String caseMatchKey,
                                             final Map<String, Integer> caseIdsByMatchKeyValue) {
        String matchKeyValue = scenarioResult.getOverview().getTestRail().getCaseMatchKeyValue().trim();
        Integer caseId = caseIdsByMatchKeyValue.get(matchKeyValue);
        if (caseId == null) {
            log.error(TestRailConstants.CASE_MATCH_KEY_VALUE_NOT_FOUND,
                    matchKeyValue, caseMatchKey, scenarioResult.getName());
            return Optional.empty();
        }
        return Optional.of(new ScenarioCase(scenarioResult, caseId));
    }
}
