package com.knubisoft.testing.framework.coordinator;

public class InboundDestination {

    private final String alias;
    private final String destinationUrl; // e.g.: https://your.api.com/webhooks/payment

    public InboundDestination(String alias, String destinationUrl) {
        this.alias = alias;
        this.destinationUrl = destinationUrl;
    }

    public String getAlias() {
        return alias;
    }

    public String getDestinationUrl() {
        return destinationUrl;
    }
}
