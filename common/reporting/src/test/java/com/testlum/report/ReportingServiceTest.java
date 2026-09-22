package com.testlum.report;

import com.testlum.testing.report.GlobalScenarioStatCollector;
import com.testlum.testing.report.ReportListener;
import com.testlum.testing.report.ReportingService;
import com.testlum.testing.report.ScenarioResult;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link ReportingService} verifying the fan-out to report listeners.
 */
class ReportingServiceTest {

    private final ReportListener failing = mock(ReportListener.class);
    private final ReportListener healthy = mock(ReportListener.class);
    private final ReportingService service = new ReportingService(List.of(failing, healthy));

    @Test
    void failingListenerDoesNotStopTheOthersOnRunStart() {
        doThrow(new IllegalStateException("boom")).when(failing).onRunStart();

        assertDoesNotThrow(service::onRunStart);

        verify(healthy).onRunStart();
    }

    @Test
    void failingListenerDoesNotStopTheOthersOnScenarioFinished() {
        final ScenarioResult result = new ScenarioResult();
        doThrow(new IllegalStateException("boom")).when(failing).onScenarioFinished(result);

        assertDoesNotThrow(() -> service.onScenarioFinished(result));

        verify(healthy).onScenarioFinished(result);
    }

    @Test
    void failingListenerDoesNotStopTheOthersOnRunFinished() {
        final GlobalScenarioStatCollector collector = new GlobalScenarioStatCollector();
        doThrow(new IllegalStateException("boom")).when(failing).onRunFinished(collector);

        assertDoesNotThrow(() -> service.onRunFinished(collector));

        verify(healthy).onRunFinished(collector);
    }
}
