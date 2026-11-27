package com.testlum.testing.framework.inbound.ngrok;

/**
 * Provider of ngrok configuration.
 * <p>
 * If {@link #isEnabled()} returns {@code false}, then the ngrok tunnel will not be started.
 */
public interface NgrokConfigProvider {

    /** Whether to start the ngrok tunnel. */
    boolean isEnabled();

    /** Ngrok auth token. */
    String authToken();

    /** Reserved domain name. If null, ngrok will generate a random one for each start. */
    String reservedDomain();

}
