package com.testlum.testing.framework.report.testrail.impl;

import com.testlum.testing.framework.report.ScenarioResult;
import com.testlum.testing.framework.report.testrail.TestRailApiClient;
import com.testlum.testing.framework.report.testrail.model.ResultRequestDto;
import com.testlum.testing.framework.report.testrail.util.TestRailFailedScreenshotCollector;
import com.testlum.testing.framework.report.testrail.util.ScenarioResultDataExtractor;
import com.testlum.testing.framework.report.testrail.util.TestRailCaseIdResolver;
import com.testlum.testing.framework.report.testrail.util.TestRailResultMapper;
import com.testlum.testing.framework.util.LogUtil;
import com.testlum.testing.model.global_config.ExtentReports;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.model.global_config.TestRailReports;
import com.testlum.testing.model.scenario.Overview;
import com.testlum.testing.model.scenario.TestRail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TestRailServiceImplTest {

    private static final String MATCH_KEY = "custom_automation_id";
    private static final int RUN_ID = 300;

    private TestRailApiClient apiClient;
    private TestRailServiceImpl service;

    @BeforeEach
    void setUp() {
        apiClient = mock(TestRailApiClient.class);
        TestRailReports testRailReports = new TestRailReports();
        testRailReports.setCaseMatchKey(MATCH_KEY);
        ExtentReports extentReports = new ExtentReports();
        extentReports.setTestRailReports(testRailReports);
        Report report = new Report();
        report.setExtentReports(extentReports);
        GlobalTestConfiguration globalConfig = new GlobalTestConfiguration();
        globalConfig.setReport(report);

        service = new TestRailServiceImpl(
                apiClient,
                new TestRailResultMapper(),
                new ScenarioResultDataExtractor(),
                new TestRailCaseIdResolver(globalConfig, apiClient),
                new TestRailFailedScreenshotCollector(),
                mock(LogUtil.class));
    }

    private ScenarioResult scenario(final String name, final boolean enabled,
                                    final String caseId, final String matchKeyValue) {
        Overview overview = new Overview();
        if (enabled || caseId != null || matchKeyValue != null) {
            TestRail testRail = new TestRail();
            testRail.setEnabled(enabled);
            testRail.setTestCaseId(caseId);
            testRail.setCaseMatchKeyValue(matchKeyValue);
            testRail.setTestRailRunId(String.valueOf(RUN_ID));
            overview.setTestRail(testRail);
        }
        ScenarioResult scenarioResult = new ScenarioResult();
        scenarioResult.setName(name);
        scenarioResult.setSuccess(true);
        scenarioResult.setOverview(overview);
        return scenarioResult;
    }

    @SuppressWarnings("unchecked")
    private List<ResultRequestDto> captureSentResults() {
        ArgumentCaptor<List<ResultRequestDto>> captor = ArgumentCaptor.forClass(List.class);
        verify(apiClient).sendResultsInBatch(eq(RUN_ID), captor.capture(), any());
        return captor.getValue();
    }

    @Nested
    class CaseIdFlow {

        @Test
        void sendsResultUnderTheDeclaredCaseId() {
            service.generateTestRailReports(List.of(scenario("login", true, "42", null)));

            List<ResultRequestDto> sent = captureSentResults();
            assertEquals(1, sent.size());
            assertEquals("42", sent.get(0).caseId());
            verify(apiClient, never()).fetchCaseIdsByMatchKey(MATCH_KEY);
        }
    }

    @Nested
    class MatchKeyFlow {

        @Test
        void sendsResultUnderTheCaseIdResolvedFromMatchKey() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("LOGIN_001", 77));

            service.generateTestRailReports(List.of(scenario("login", true, null, "LOGIN_001")));

            List<ResultRequestDto> sent = captureSentResults();
            assertEquals(1, sent.size());
            assertEquals("77", sent.get(0).caseId());
        }

        @Test
        void bothFlowsReachTheSameRun() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of("LOGIN_001", 77));

            service.generateTestRailReports(List.of(
                    scenario("byId", true, "42", null),
                    scenario("byKey", true, null, "LOGIN_001")));

            List<String> caseIds = captureSentResults().stream().map(ResultRequestDto::caseId).sorted().toList();
            assertEquals(List.of("42", "77"), caseIds);
        }
    }

    @Nested
    class Skipped {

        @Test
        void scenarioWithoutTestRailElementDoesNotFailTheReport() {
            service.generateTestRailReports(List.of(
                    scenario("noTestRail", false, null, null),
                    scenario("byId", true, "42", null)));

            List<ResultRequestDto> sent = captureSentResults();
            assertEquals(1, sent.size());
            assertEquals("42", sent.get(0).caseId());
        }

        @Test
        void nothingIsSentWhenNoScenarioResolvesToACase() {
            when(apiClient.fetchCaseIdsByMatchKey(MATCH_KEY)).thenReturn(Map.of());

            service.generateTestRailReports(List.of(scenario("login", true, null, "MISSING")));

            verify(apiClient, never()).sendResultsInBatch(anyInt(), anyList(), any());
        }
    }
}
