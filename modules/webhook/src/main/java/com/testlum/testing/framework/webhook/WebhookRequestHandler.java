package com.testlum.testing.framework.webhook;

import com.testlum.testing.framework.inbound.server.dto.InboundRequest;
import com.testlum.testing.framework.inbound.server.dto.InboundResponse;
import com.testlum.testing.framework.inbound.server.handler.InboundRequestHandler;
import com.testlum.testing.framework.inbound.server.util.InboundResponseFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 * Handles requests to the inbound server. A request to a webhook integration path is buffered.
 */
@Slf4j
public class WebhookRequestHandler implements InboundRequestHandler {

    private final WebhookIntegrationRegistry registry;
    private final WebhookBuffer buffer;

    public WebhookRequestHandler(final WebhookIntegrationRegistry registry, final WebhookBuffer buffer) {
        this.registry = registry;
        this.buffer = buffer;
    }

    @Override
    public InboundResponse handle(final InboundRequest request) {
        Optional<WebhookEndpoint> endpoint = registry.findByRequestPath(request.getPath());
        if (endpoint.isEmpty()) {
            log.warn("Inbound request {} doesn't match any webhook integration url", request);
            return handlerNotFound(request);
        }
        log.info("Webhook received on {}: {}", endpoint.get(), request);
        BufferedWebhook webhook = buffer.add(request, endpoint.get());
        int status = webhook.awaitReplyStatus();
        log.info("Webhook {} answered with status {}", request, status);
        return InboundResponseFactory.status(status);
    }

    private InboundResponse handlerNotFound(final InboundRequest request) {
        return InboundResponseFactory.notFound(
                String.format("No webhook integration for %s %s", request.getMethod(), request.getPath()));
    }
}
