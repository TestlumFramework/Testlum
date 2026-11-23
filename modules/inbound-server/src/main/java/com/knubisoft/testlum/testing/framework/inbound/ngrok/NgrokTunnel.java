package com.knubisoft.testlum.testing.framework.inbound.ngrok;

import com.knubisoft.testlum.testing.framework.exception.DefaultFrameworkException;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.ngrok.Forwarder;
import com.ngrok.HttpBuilder;
import com.ngrok.Session;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Represents a NgrokTunnel for forwarding requests to a local server.
 * <p>
 * It starts only when Ngrok is enabled in the global configuration file.
 * If so, it starts after {@link InboundServer}.
 */
@Slf4j
public class NgrokTunnel {

    private final NgrokConfigProvider config;
    private final InboundServer localServer;
    private final AtomicBoolean isRunning = new AtomicBoolean(false);
    private volatile Session session;
    private volatile Forwarder forwarder;
    @Getter
    private volatile String publicUrl;

    public NgrokTunnel(NgrokConfigProvider config, InboundServer localServer) {
        this.config = config;
        this.localServer = localServer;
    }

    public boolean isRunning() {
        return isRunning.get();
    }

    /** Starts the ngrok tunnel for forwarding inbound requests to the local server.*/
    public synchronized void start() {
        if (isRunning.get()) {
            return;
        }
        if (!localServer.isRunning()) {
            throw new DefaultFrameworkException("Local inbound server must be started before ngrok tunnel");
        }
        try {
            Session.Builder sessionBuilder = (config.authToken() == null || config.authToken().isBlank())
                    ? Session.withAuthtokenFromEnv()
                    : Session.withAuthtoken(config.authToken());
            this.session = sessionBuilder
                    .metadata("testlum-inbound")
                    .connect();
            HttpBuilder listenerBuilder = session.httpEndpoint().metadata("testlum-webhook-listener");
            if (config.reservedDomain() != null && !config.reservedDomain().isBlank()) {
                listenerBuilder = listenerBuilder.domain(config.reservedDomain());
            }

            String localHost = "0.0.0.0".equals(localServer.getHost()) ? "127.0.0.1" : localServer.getHost();
            String target = "http://" + localHost + ":" + localServer.getPort();
            Forwarder.Endpoint endpoint = listenerBuilder.forward(new URL(target));
            this.forwarder = endpoint;
            this.publicUrl = endpoint.getUrl();

            isRunning.set(true);

            log.info("============================================================");
            log.info(" ngrok tunnel STARTED ");
            log.info("    public URL: " + publicUrl);
            log.info("    forward to: " + target);
            log.info("============================================================");
        } catch (Exception e) {
            safeClose();
            throw new DefaultFrameworkException("Failed to start ngrok tunnel: " + e.getMessage()
                            + ((e.getMessage() != null && e.getMessage().toLowerCase().contains("authtoken"))
                            ? " (check ngrok auth token in config or NGROK_AUTHTOKEN env var)" : ""), e);
        }
    }

    public synchronized void stop() {
        if (!isRunning.compareAndSet(true, false)) {
            return;
        }
        safeClose();
    }

    private void safeClose() {
        closeQuietly(forwarder);
        closeQuietly(session);
        forwarder = null;
        session = null;
        publicUrl = null;
    }

    private static void closeQuietly(AutoCloseable c) {
        if (c == null) {
            return;
        }
        try {
            c.close();
        } catch (Exception ignored) {

        }
    }
}
