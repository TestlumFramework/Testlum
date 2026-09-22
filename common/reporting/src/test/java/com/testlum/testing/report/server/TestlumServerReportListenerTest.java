package com.testlum.testing.report.server;

import com.testlum.reporting.sdk.client.ReportPublisher;
import com.testlum.reporting.sdk.client.TestlumReportClient;
import com.testlum.reporting.sdk.exception.ReportPublishException;
import com.testlum.reporting.sdk.model.config.ProjectConfigSnapshot;
import com.testlum.reporting.sdk.model.launch.LaunchFinished;
import com.testlum.reporting.sdk.model.launch.LaunchStarted;
import com.testlum.reporting.sdk.model.launch.LaunchStatus;
import com.testlum.reporting.sdk.model.scenario.ScenarioReport;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.Report;
import com.testlum.testing.report.GlobalScenarioStatCollector;
import com.testlum.testing.report.ScenarioResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link TestlumServerReportListener} verifying launch streaming, soft failure handling
 * and the interrupted launch.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestlumServerReportListenerTest {

    private final GlobalTestConfiguration globalTestConfiguration = new GlobalTestConfiguration();
    private final Report report = new Report();
    private final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
    @Mock
    private TestlumReportClientFactory clientFactory;
    @Mock
    private TestlumReportClient client;
    @Mock
    private ReportPublisher publisher;
    @Mock
    private ProjectConfigSnapshotMapper projectConfigSnapshotMapper;
    private TestlumServerReportListener listener;

    private static ScenarioResult scenario(final int id, final boolean success) {
        ScenarioResult result = new ScenarioResult();
        result.setId(id);
        result.setPath("scenario-" + id + ".xml");
        result.setSuccess(success);
        result.setCommands(List.of());
        return result;
    }

    @BeforeEach
    void setUp() {
        report.setProjectName("demo");
        globalTestConfiguration.setReport(report);
        when(clientFactory.create(report)).thenReturn(client);
        when(client.openPublisher()).thenReturn(publisher);
        when(publisher.getLaunchId()).thenReturn("launch-1");
        when(projectConfigSnapshotMapper.map()).thenReturn(ProjectConfigSnapshot.builder().build());
        listener = new TestlumServerReportListener(globalTestConfiguration, clientFactory,
                new ScenarioReportMapper(), projectConfigSnapshotMapper);
    }

    @Test
    void streamsLaunchInOrder() {
        runScenarios(true, false);

        InOrder order = inOrder(publisher);
        order.verify(publisher).publishLaunchStarted(any(LaunchStarted.class));
        order.verify(publisher).publishProjectConfig(any(ProjectConfigSnapshot.class));
        order.verify(publisher, times(2)).publishScenario(any(ScenarioReport.class));
        order.verify(publisher).publishLaunchFinished(any(LaunchFinished.class));
        order.verify(publisher).close();
    }

    @Test
    void finishesFailedLaunchWithTotals() {
        runScenarios(true, false);

        LaunchFinished finished = launchFinished();
        assertEquals(LaunchStatus.FAILED, finished.getStatus());
        assertEquals(2, finished.getTotal());
        assertEquals(1, finished.getPassed());
        assertEquals(1, finished.getFailed());
    }

    @Test
    void countsSkippedScenariosSeparatelyAndStillPublishesThem() {
        report.setOnlyFailedScenarios(true);
        listener.onRunStart();
        ScenarioResult skipped = scenario(1, false);
        skipped.setSkipped(true);
        listener.onScenarioFinished(skipped);
        listener.onRunFinished(collector);

        verify(publisher).publishScenario(any(ScenarioReport.class));
        LaunchFinished finished = launchFinished();
        assertEquals(LaunchStatus.PASSED, finished.getStatus());
        assertEquals(1, finished.getSkipped());
        assertEquals(1, finished.getTotal());
    }

    @Test
    void finishesPassedLaunch() {
        runScenarios(true, true);

        assertEquals(LaunchStatus.PASSED, launchFinished().getStatus());
    }

    @Test
    void publishesOnlyFailedScenariosWhenConfiguredButCountsAll() {
        report.setOnlyFailedScenarios(true);

        runScenarios(true, true, false);

        ArgumentCaptor<ScenarioReport> scenarios = ArgumentCaptor.forClass(ScenarioReport.class);
        verify(publisher).publishScenario(scenarios.capture());
        assertEquals(3, scenarios.getValue().getId());
        assertEquals(3, launchFinished().getTotal());
    }

    @Test
    void publishFailureDoesNotBreakTheRun() {
        doThrow(new ReportPublishException("scenario-report", new IllegalStateException("broker down")))
                .when(publisher).publishScenario(any(ScenarioReport.class));

        assertDoesNotThrow(() -> runScenarios(false));

        verify(publisher).publishLaunchFinished(any(LaunchFinished.class));
        verify(publisher).close();
    }

    @Test
    void interruptedRunClosesLaunchAsInterruptedOnlyOnce() {
        listener.onRunStart();
        listener.onScenarioFinished(scenario(1, true));

        listener.onInterrupted();
        listener.onRunFinished(collector);

        assertEquals(LaunchStatus.INTERRUPTED, launchFinished().getStatus());
        verify(publisher, never()).close();
    }

    @Test
    void runFinishWithoutStartDoesNothing() {
        listener.onRunFinished(collector);

        verifyNoInteractions(publisher);
    }

    private void runScenarios(final boolean... outcomes) {
        listener.onRunStart();
        for (int i = 0; i < outcomes.length; i++) {
            listener.onScenarioFinished(scenario(i + 1, outcomes[i]));
        }
        listener.onRunFinished(collector);
    }

    private LaunchFinished launchFinished() {
        ArgumentCaptor<LaunchFinished> captor = ArgumentCaptor.forClass(LaunchFinished.class);
        verify(publisher).publishLaunchFinished(captor.capture());
        return captor.getValue();
    }
}
