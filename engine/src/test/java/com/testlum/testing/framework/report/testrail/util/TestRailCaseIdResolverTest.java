package com.testlum.testing.framework.report.testrail.util;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.api.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.TestRailConstants;
import com.testlum.testing.framework.report.testrail.service.util.TestRailCaseIdResolver;
import com.testlum.testing.framework.report.testrail.summary.dto.NotReportedCase;
import com.testlum.testing.framework.report.testrail.service.model.ScenarioCase;
import com.testlum.testing.framework.report.testrail.summary.TestRailReportSummary;
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
    private TestRailReportSummary summary;

    @BeforeEach
    void setUp() {
        apiClient = mock(TestRailApiClient.class);
        summary = new TestRailReportSummary();
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

    private List<ScenarioCase> resolveCases(final TestRailCaseIdResolver resolver,
                                           final List<ScenarioResult> scenarioResults) {
        summary = new TestRailReportSummary();
        return resolver.resolveCases(scenarioResults, summary);
    }

    private List<String> notReportedScenarios() {
        return summary.getNotReportedCases().stream().map(NotReportedCase::scenarioName).toList();
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

            List<ScenarioCase> resolved = resolveCases(resolver,
                    List.of(scenario("login", "42", null)));

            assertEquals(1, resolved.size());
            assertEquals(42, resolved.get(0).caseId());
            verify(apiClient, never()).fetchCaseIdsByMatchKey(anyString());
        }

        @Test
        void resolvesEveryCaseIdOfCommaSeparatedList() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolved = resolveCases(resolver,
                    List.of(scenario("login", "91, 94", null)));

            assertEquals(2, resolved.size());
            assertEquals(List.of(91, 94), resolved.stream().map(ScenarioCase::caseId).toList());
            assertTrue(resolved.stream().allMatch(scenarioCase ->
                    "login".equals(scenarioCase.scenarioResult().getName())));
            verify(apiClient, never()).fetchCaseIdsByMatchKey(anyString());
        }

        @Test
        void reportsDuplicatedCaseIdOnlyOnce() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolved = resolveCases(resolver,
                    List.of(scenario("login", "91,91", null)));

            assertEquals(1, resolved.size());
            assertEquals(91, resolved.get(0).caseId());
        }

        @Test
        void skipsUnparsableEntryAndKeepsRemainingCaseIds() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolved = resolveCases(resolver,
                    List.of(scenario("login", "91,C94,0,94", null)));

            assertEquals(List.of(91, 94), resolved.stream().map(ScenarioCase::caseId).toList());
        }

        @Test
        void skipsScenarioWithNeitherCaseIdNorMatchKeyValue() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            assertTrue(resolveCases(resolver, List.of(scenario("login", null, null))).isEmpty());
            assertEquals(List.of("login"), notReportedScenarios());
            assertEquals(TestRailConstants.REASON_CASE_REFERENCE_MISSING,
                    summary.getNotReportedCases().get(0).reason());
        }

        @Test
        void recordsUnparsableEntryAsNotReportedCase() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            resolveCases(resolver, List.of(scenario("login", "91,C94", null)));

            assertEquals(1, summary.getNotReportedCases().size());
            assertEquals("C94", summary.getNotReportedCases().get(0).caseId());
            assertEquals(TestRailConstants.REASON_CASE_ID_NOT_PARSABLE,
                    summary.getNotReportedCases().get(0).reason());
        }

        @Test
        void skipsScenarioWithNonNumericCaseIdAndNoMatchKeyValue() {
            TestRailCaseIdResolver resolver = resolverWithMatchKey(MATCH_KEY);

            assertTrue(resolveCases(resolver, List.of(scenario("login", "C42", null))).isEmpty());
        }
    }

    @Nested
    class MatchKeyFlow {

        @Test
        void resolvesCaseIdFromConfiguredMatchKey() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("LOGIN_001", 77));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolvedCases = resolveCases(caseResolver,
                    List.of(scenario("login", null, "LOGIN_001")));

            assertEquals(1, resolvedCases.size());
            assertEquals(77, resolvedCases.get(0).caseId());
        }

        @Test
        void fetchesCasesOnlyOnceForManyScenarios() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("A", 1, "B", 2));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolvedCases = resolveCases(caseResolver, List.of(
                    scenario("a", null, "A"),
                    scenario("b", null, "B")));

            assertEquals(2, resolvedCases.size());
            verify(apiClient).fetchCaseIdsByMatchKey(MATCH_KEY);
        }

        @Test
        void skipsScenarioWhenMatchKeyValueHasNoCase() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("OTHER", 5));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            assertTrue(resolveCases(caseResolver,
                    List.of(scenario("login", null, "LOGIN_001"))).isEmpty());
            assertEquals(List.of("login"), notReportedScenarios());
            assertEquals(String.format(TestRailConstants.REASON_MATCH_KEY_VALUE_NOT_FOUND, "LOGIN_001", MATCH_KEY),
                    summary.getNotReportedCases().get(0).reason());
        }

        @Test
        void skipsScenarioAndDoesNotCallApiWhenCaseMatchKeyNotConfigured() {
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(null);

            assertTrue(resolveCases(caseResolver,
                    List.of(scenario("login", null, "LOGIN_001"))).isEmpty());
            verify(apiClient, never()).fetchCaseIdsByMatchKey(anyString());
            assertEquals(TestRailConstants.REASON_MATCH_KEY_NOT_CONFIGURED,
                    summary.getNotReportedCases().get(0).reason());
        }
    }

    @Nested
    class MixedFlows {

        @Test
        void resolvesBothFlowsInOneRun() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("LOGIN_001", 77));
            TestRailCaseIdResolver caseResolver = resolverWithMatchKey(MATCH_KEY);

            List<ScenarioCase> resolvedCases = resolveCases(caseResolver, List.of(
                    scenario("byId", "42", null),
                    scenario("byKey", null, "LOGIN_001"),
                    scenario("broken", null, null)));

            assertEquals(2, resolvedCases.size());
            assertTrue(resolvedCases.stream().anyMatch(scenarioCase -> scenarioCase.caseId() == 42));
            assertTrue(resolvedCases.stream().anyMatch(scenarioCase -> scenarioCase.caseId() == 77));
        }
    }
}
