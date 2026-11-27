package com.testlum.testing.framework.report.testrail.api.dto;

public record TestRailDeliveryOutcome(boolean delivered, String failureReason, int attachedScreenshots) {

    public static TestRailDeliveryOutcome delivered(final int attachedScreenshots) {
        return new TestRailDeliveryOutcome(true, null, attachedScreenshots);
    }

    public static TestRailDeliveryOutcome failed(final String failureReason) {
        return new TestRailDeliveryOutcome(false, failureReason, 0);
    }
}
