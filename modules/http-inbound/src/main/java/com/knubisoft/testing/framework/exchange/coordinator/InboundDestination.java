package com.knubisoft.testing.framework.exchange.coordinator;

public class InboundDestination {

    private final String alias;
    private final String endpoint; // e.g.: /webhooks/payment
    private final String destinationUrl; // e.g.: https://your.api.com/webhooks/payment

    public InboundDestination(String alias, String endpoint, String destinationUrl) {
        this.alias = alias;
        this.endpoint = endpoint;
        this.destinationUrl = destinationUrl;
    }

    public String getAlias() {
        return alias;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }
}
