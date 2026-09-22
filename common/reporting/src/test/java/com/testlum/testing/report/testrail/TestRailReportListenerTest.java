package com.testlum.testing.report.testrail;

import com.testlum.testing.report.GlobalScenarioStatCollector;
import com.testlum.testing.report.testrail.service.TestRailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for {@link TestRailReportListener} verifying that results are delivered to TestRail
 * at the end of the run and that TestRail failures never break the run.
 */
@ExtendWith(MockitoExtension.class)
class TestRailReportListenerTest {

    @Mock
    private TestRailService testRailService;

    @InjectMocks
    private TestRailReportListener listener;

    @Test
    void sendsCollectedResultsOnRunFinished() {
        final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();

        listener.onRunFinished(collector);

        verify(testRailService).generateTestRailReports(collector.getResults());
    }

    @Test
    void testRailFailureDoesNotFailRunFinish() {
        doThrow(new RuntimeException("TestRail is down"))
                .when(testRailService).generateTestRailReports(any());

        assertDoesNotThrow(() -> listener.onRunFinished(new GlobalScenarioStatCollector()));
        verify(testRailService).generateTestRailReports(any());
    }
}
