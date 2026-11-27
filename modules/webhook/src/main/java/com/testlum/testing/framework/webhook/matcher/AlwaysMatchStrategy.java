package com.testlum.testing.framework.webhook.matcher;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;

/**
 * For testing without parallel execution.
 */
public class AlwaysMatchStrategy implements WebhookMatchStrategy {


    @Override
    public boolean matches(InboundRequest request) {
        return true;
    }
}
