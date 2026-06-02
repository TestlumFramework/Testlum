package com.knubisoft.testlum.testing.framework.configuration.inbound;

import com.knubisoft.testing.framework.forwarder.DestinationForwarder;
import com.knubisoft.testing.framework.producer.ResponseProducerFactory;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookCoordinator;
import com.knubisoft.testlum.testing.framework.coordinator.WebhookExecutorProvider;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.InboundRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.handler.impl.RegistryRequestHandler;
import com.knubisoft.testlum.testing.framework.inbound.server.server.InboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.server.impl.JdkInboundServer;
import com.knubisoft.testlum.testing.framework.inbound.server.subscription.SubscriptionRegistry;
import com.knubisoft.testlum.testing.framework.interpreter.lib.http.ApiClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

@Configuration
public class WebhookConfiguration {

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
                                       @Value("${testlum.webhook.host:0.0.0.0}") String host,
                                       @Value("${testlum.webhook.port:8080}") int port,
                                       @Value("${testlum.webhook.maxThreads:200}") int maxThreads) {
        return new JdkInboundServer(host, port, maxThreads, handler);
    }

    @Bean
    public DestinationForwarder destinationForwarder(ApiClient apiClient) {
        return new DestinationForwarder(apiClient);
    }

    @Bean
    public ResponseProducerFactory responseProducerFactory(DestinationForwarder destinationForwarder) {
        return new ResponseProducerFactory(destinationForwarder);
    }

    @Bean(initMethod = "start", destroyMethod = "stop")
    public WebhookCoordinator webhookCoordinator(InboundServer server, SubscriptionRegistry registry) {
        return new WebhookCoordinator(server, registry);
    }

    /**
     * Executor for webhook await request calls. Cached thread pool, because
     * there could be many parallel scenarios with webhook steps.
     */
    @Bean(destroyMethod = "shutdown")
    public ExecutorService webhookSessionExecutor() {
        AtomicLong n = new AtomicLong();
        return Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "testlum-webhook-session-" + n.incrementAndGet());
            t.setDaemon(true);
            return t;
        });
    }

    @Bean
    public WebhookExecutorProvider webhookExecutorProvider(ExecutorService webhookSessionExecutor) {
        return () -> webhookSessionExecutor;
    }

}
