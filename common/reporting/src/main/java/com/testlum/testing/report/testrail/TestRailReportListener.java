package com.testlum.testing.report.testrail;

import com.testlum.testing.report.GlobalScenarioStatCollector;
import com.testlum.testing.report.ReportListener;
import com.testlum.testing.report.testrail.service.TestRailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Slf4j
@Lazy
@RequiredArgsConstructor
@Component
public class TestRailReportListener implements ReportListener {

    private final TestRailService testRailService;

    @Override
    public void onRunFinished(final GlobalScenarioStatCollector collector) {
        try {
            testRailService.generateTestRailReports(collector.getResults());
        } catch (Exception e) {
            log.error(TestRailConstants.LOG_REPORTING_ERROR, e.getMessage(), e);
        }
    }
}
