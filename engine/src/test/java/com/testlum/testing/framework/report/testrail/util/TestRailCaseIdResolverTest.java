package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.model.ScenarioCase;
import com.testlum.testing.model.global_config.ExtentReports;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.model.global_config.TestRailReports;
import com.testlum.testing.model.scenario.Overview;
import com.testlum.testing.model.scenario.TestRail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestRailCaseIdResolverTest {

    private static final String MATCH_KEY = "custom_automation_id";

    private TestRailApiClient apiClient;

    @BeforeEach
    void setUp() {
        apiClient = mock(TestRailApiClient.class);
    }

    private TestRailCaseIdResolver resolverWithMatchKey(final String caseMatchKey) {
        TestRailReports testRailReports = new TestRailReports();
        testRailReports.setCaseMatchKey(caseMatchKey);
        ExtentReports extentReports = new ExtentReports();
        extentReports.setTestRailReports(testRailReports);
        Report report = new Report();
        report.setExtentReports(extentReports);
        GlobalTestConfiguration globalConfig = new GlobalTestConfiguration();
        globalConfig.setReport(report);
        return new TestRailCaseIdResolver(globalConfig, apiClient);
    }

    private ScenarioResult scenario(final String name, final String caseId, final String matchKeyValue) {
        TestRail testRail = new TestRail();
        testRail.setEnabled(true);
        testRail.setTestCaseId(caseId);
        testRail.setCaseMatchKeyValue(matchKeyValue);
        Overview overview = new Overview();
        overview.setTestRail(testRail);
        ScenarioResult scenarioResult = new ScenarioResult();
        scenarioResult.setName(name);
        scenarioResult.setOverview(overview);
        return scenarioResult;
    }

    @Nested
    class CaseIdFlow {

        @Test
        void resolvesDirectlyFromTestCaseIdWithoutCallingApi() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolved = resolver.resolveCases(
                    List.of(scenario("login", "42", null)));

            assertEquals(1, resolved.size());
            assertEquals(42, resolved.get(0).caseId());
            verify(apiClient, never()).fetchCaseIdsByMatchKey(anyString());
        }

        @Test
        void skipsScenarioWithNeitherCaseIdNorMatchKeyValue() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            assertTrue(resolver.resolveCases(List.of(scenario("login", null, null))).isEmpty());
        }

        @Test
        void skipsScenarioWithNonNumericCaseIdAndNoMatchKeyValue() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            assertTrue(resolver.resolveCases(List.of(scenario("login", "C42", null))).isEmpty());
        }
    }

    @Nested
    class MatchKeyFlow {

        @Test
        void resolvesCaseIdFromConfiguredMatchKey() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("LOGIN_001", 77));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolvedCases = caseResolver.resolveCases(
                    List.of(scenario("login", null, "LOGIN_001")));

            assertEquals(1, resolvedCases.size());
            assertEquals(77, resolvedCases.get(0).caseId());
        }

        @Test
        void fetchesCasesOnlyOnceForManyScenarios() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("A", 1, "B", 2));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolvedCases = caseResolver.resolveCases(List.of(
                    scenario("a", null, "A"),
                    scenario("b", null, "B")));

            assertEquals(2, resolvedCases.size());
            verify(apiClient).fetchCaseIdsByMatchKey(MATCH_KEY);
        }

        @Test
        void skipsScenarioWhenMatchKeyValueHasNoCase() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("OTHER", 5));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            assertTrue(caseResolver.resolveCases(
                    List.of(scenario("login", null, "LOGIN_001"))).isEmpty());
        }

        @Test
        void skipsScenarioAndDoesNotCallApiWhenCaseMatchKeyNotConfigured() {
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(null);

            assertTrue(caseResolver.resolveCases(
                    List.of(scenario("login", null, "LOGIN_001"))).isEmpty());
            verify(apiClient, never()).fetchCaseIdsByMatchKey(anyString());
        }
    }

    @Nested
    class MixedFlows {

        @Test
        void resolvesBothFlowsInOneRun() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("LOGIN_001", 77));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolvedCases = caseResolver.resolveCases(List.of(
                    scenario("byId", "42", null),
                    scenario("byKey", null, "LOGIN_001"),
                    scenario("broken", null, null)));

            assertEquals(2, resolvedCases.size());
            assertTrue(resolvedCases.stream().anyMatch(scenarioCase -> scenarioCase.caseId() == 42));
            assertTrue(resolvedCases.stream().anyMatch(scenarioCase -> scenarioCase.caseId() == 77));
        }
    }
}
