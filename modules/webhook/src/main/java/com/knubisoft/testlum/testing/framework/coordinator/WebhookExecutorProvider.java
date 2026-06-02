package com.knubisoft.testlum.testing.framework.coordinator;

import java.util.concurrent.ExecutorService;

public interface WebhookExecutorProvider {

    ExecutorService executor();

}
