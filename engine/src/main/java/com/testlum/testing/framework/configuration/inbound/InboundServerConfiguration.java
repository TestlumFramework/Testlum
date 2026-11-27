package com.testlum.testing.framework.configuration.inbound;

import com.testlum.testing.framework.forwarder.DestinationForwarder;
import com.testlum.testing.framework.producer.ResponseProducerFactory;
import com.testlum.testing.framework.inbound.InboundServerLifecycle;
import com.testlum.testing.framework.inbound.ngrok.NgrokTunnel;
import com.testlum.testing.framework.inbound.server.handler.InboundRequestHandler;
import com.testlum.testing.framework.inbound.server.server.InboundServer;
import com.testlum.testing.framework.inbound.server.server.impl.JdkInboundServer;
import com.testlum.testing.framework.interpreter.lib.http.ApiClient;
import com.testlum.testing.framework.webhook.WebhookBuffer;
import com.testlum.testing.framework.webhook.WebhookIntegrationRegistry;
import com.testlum.testing.framework.webhook.WebhookRequestHandler;
import com.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.testlum.testing.model.global_config.HttpServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InboundServerConfiguration {

    @Bean
    public InboundRequestHandler inboundRequestHandler(final WebhookIntegrationRegistry webhookIntegrationRegistry,
                                                       final WebhookBuffer webhookBuffer) {
        return new WebhookRequestHandler(webhookIntegrationRegistry, webhookBuffer);
    }

    @Bean
    public InboundServer inboundServer(final InboundRequestHandler handler,
                                       final GlobalTestConfiguration globalTestConfiguration) {
        HttpServer httpServer = globalTestConfiguration.getHttpServer();
        if (httpServer == null) {
            return defaultInboundServer(handler);
        }
        return new JdkInboundServer(
                httpServer.isEnabled(), httpServer.getHost(), httpServer.getPort(),
                httpServer.getMaxThreads(), handler);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public InboundServerLifecycle inboundServerLifecycle(final InboundServer inboundServer,
                                                         @Autowired(required = false) final NgrokTunnel ngrokTunnel,
                                                         final GlobalTestConfiguration globalTestConfiguration) {
        HttpServer httpServer = globalTestConfiguration.getHttpServer();
        boolean enabled = httpServer != null && httpServer.isEnabled();
        return new InboundServerLifecycle(enabled, inboundServer, ngrokTunnel);
    }

    @Bean
    public DestinationForwarder destinationForwarder(final ApiClient apiClient) {
        return new DestinationForwarder(apiClient);
    }

    @Bean
    public ResponseProducerFactory responseProducerFactory(final DestinationForwarder destinationForwarder) {
        return new ResponseProducerFactory(destinationForwarder);
    }

    private JdkInboundServer defaultInboundServer(final InboundRequestHandler handler) {
        return new JdkInboundServer(
                false, "127.0.0.1", 58743, 50, handler);
    }

}
