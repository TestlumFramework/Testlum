package com.knubisoft.testlum.starter.failure;

import org.springframework.boot.context.event.ApplicationFailedEvent;
import org.springframework.context.ApplicationListener;

public class StartupFailureListener implements ApplicationListener<ApplicationFailedEvent> {

    private static final String HEADLINE = "Testlum failed to start. Application context was not initialized";

    @Override
    public void onApplicationEvent(final ApplicationFailedEvent event) {
        StartupFailureReporter.report(HEADLINE, event.getException());
    }

}
