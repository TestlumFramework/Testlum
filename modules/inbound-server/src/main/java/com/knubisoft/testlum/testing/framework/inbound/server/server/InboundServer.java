package com.knubisoft.testlum.testing.framework.inbound.server.server;

import java.time.Duration;

public interface InboundServer {

    void start();

    void stop(Duration grace);

    int getPort();

    String getHost();

    boolean isRunning();

}
