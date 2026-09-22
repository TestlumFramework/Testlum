package com.testlum.testing.report;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class ReportingService {

    private final List<ReportListener> listeners;

    public void onRunStart() {
        notifyEach("run start", ReportListener::onRunStart);
    }

    public void onScenarioFinished(final ScenarioResult result) {
        notifyEach("scenario finish", listener -> listener.onScenarioFinished(result));
    }

    public void onRunFinished(final GlobalScenarioStatCollector collector) {
        notifyEach("run finish", listener -> listener.onRunFinished(collector));
    }

    private void notifyEach(final String event, final Consumer<ReportListener> action) {
        for (ReportListener listener : listeners) {
            try {
                action.accept(listener);
            } catch (Exception e) {
                log.error("Report listener {} failed on {}", listener.getClass().getSimpleName(), event, e);
            }
        }
    }
}
