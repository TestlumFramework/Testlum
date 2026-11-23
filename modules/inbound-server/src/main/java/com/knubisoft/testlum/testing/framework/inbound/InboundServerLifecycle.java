package com.knubisoft.testlum.testing.framework.inbound;

import com.knubisoft.testlum.testing.framework.inbound.ngrok.NgrokTunnel;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;

/**
 * Starts and stops the inbound HTTP server together with the optional ngrok tunnel.
 * <p>
 * When the server is disabled in the global config, nothing is started
 */
@Slf4j
public class InboundServerLifecycle {

    private static final Duration STOP_GRACE = Duration.ofSeconds(5);

    private final boolean enabled;
    private final InboundServer server;
    private final NgrokTunnel ngrokTunnel;

    public InboundServerLifecycle(final boolean enabled,
                                  final InboundServer server,
                                  final NgrokTunnel ngrokTunnel) {
        this.enabled = enabled;
        this.server = server;
        this.ngrokTunnel = ngrokTunnel;
    }

    public void start() {
        if (!enabled) {
            log.debug("Inbound HTTP server is disabled; not starting it");
            return;
        }
        server.start();
        if (ngrokTunnel != null) {
            ngrokTunnel.start();
        }
    }

    public void stop() {
        if (ngrokTunnel != null) {
            ngrokTunnel.stop();
        }
        server.stop(STOP_GRACE);
    }
}
