package com.testlum.testing.report;

public interface ReportListener {

    default void onRunStart() {
    }

    default void onScenarioFinished(final ScenarioResult result) {
    }

    default void onRunFinished(final GlobalScenarioStatCollector collector) {
    }
}
