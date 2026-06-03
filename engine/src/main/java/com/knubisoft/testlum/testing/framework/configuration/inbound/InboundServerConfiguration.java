package com.knubisoft.testlum.testing.framework.configuration.inbound;

import com.knubisoft.testing.framework.forwarder.DestinationForwarder;
import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.InboundRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.impl.RegistryRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.server.impl.JdkInboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiClient;
import com.knubisoft.testlum.testing.model.global_config.GlobalTestConfiguration;
import com.knubisoft.testlum.testing.model.global_config.HttpServer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InboundServerConfiguration {

    @Bean
    public SubscriptionRegistry subscriptionRegistry() {
        return new SubscriptionRegistry(true);
    }

    @Bean
    public InboundRequestHandler inboundRequestHandler(SubscriptionRegistry subscriptionRegistry) {
        return new RegistryRequestHandler(subscriptionRegistry);
    }

    @Bean
    public InboundServer inboundServer(InboundRequestHandler handler,
                                       GlobalTestConfiguration globalTestConfiguration) {
        HttpServer httpServer = globalTestConfiguration.getHttpServer();
        if (httpServer == null) {
            return defaultInboundServer(handler);
        }
        return new JdkInboundServer(
                httpServer.isEnabled(), httpServer.getHost(), httpServer.getPort(),
                httpServer.getMaxThreads(), handler);
    }

    @Bean
    public DestinationForwarder destinationForwarder(ApiClient apiClient) {
        return new DestinationForwarder(apiClient);
    }

    @Bean
    public ResponseProducerFactory responseProducerFactory(DestinationForwarder destinationForwarder) {
        return new ResponseProducerFactory(destinationForwarder);
    }

    private JdkInboundServer defaultInboundServer(InboundRequestHandler handler) {
        return new JdkInboundServer(
                false, "127.0.0.1", 58743, 50, handler);
    }

}
